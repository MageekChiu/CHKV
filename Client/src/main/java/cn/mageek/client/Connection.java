package cn.mageek.client;

import cn.mageek.client.handler.DataHandler;
import cn.mageek.client.handler.WatchHandler;
import cn.mageek.common.ha.HAThirdParty;
import cn.mageek.common.ha.ZKThirdParty;
import cn.mageek.common.model.DataRequest;
import cn.mageek.common.model.DataResponse;
import cn.mageek.common.model.WatchRequest;
import cn.mageek.common.util.Encoder;
import cn.mageek.common.util.HAHelper;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Consumer;

import static cn.mageek.common.model.LineType.SINGLE_ERROR;
import static cn.mageek.common.util.ConsistHash.getServer;

/**
 * Client直接发送命令即可，由Connection保存有效DataNode并负责计算具体的DataNode然后发起连接与请求，
 * @author Mageek Chiu
 * @date 2018/5/10 0010:19:33
 */
public class Connection implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(Connection.class);
    private static final WatchRequest watchRequest = new WatchRequest(false);// 心跳是不需要立即回复的// 所有实例共享，所以静态节省创建时间
    private static final DataResponse noDataNode = new DataResponse(SINGLE_ERROR,"no DataNode available");// 所有实例共享，所以静态节省创建时间

    public ConcurrentSkipListMap<Integer, String> sortedServerMap =  new ConcurrentSkipListMap<>();// DataNode hash 环，可以被handler修改

    private Channel nameNodeChannel = null;// NameNode Channel
    private Map<String,Channel> dataNodeChannelList = new ConcurrentHashMap<>();// ip:port -> channel // 保存DataNode连接
    private Thread heartbeat;// NameNode心跳线程
    private volatile boolean running = true;// 是否继续运行
    private volatile Map<String ,DataResponse> dataResponseMap = new ConcurrentHashMap<>(1024);// 存放所有响应，用于和发出的命令对应

    private String nameNodeIP;
    private String nameNodePort;
    private EventLoopGroup group;

    private boolean useHA = true ;
    private HAThirdParty party = null;

    /**
     * 仅仅构造
     */
    Connection() { }

    /**
     * 构造并且连接NameNode
     * @param nameNodeIP nameNodePort
     * @param nameNodePort nameNodePort
     */
    Connection(String nameNodeIP,String nameNodePort) {
        connect(nameNodeIP,nameNodePort);
    }

    Connection(String nameNodeIP,String nameNodePort,boolean useHA) {
        connect(nameNodeIP,nameNodePort,useHA);
    }


    public void connect(String nameNodeIP,String nameNodePort,boolean useHA){
        this.useHA = useHA;
        connect(nameNodeIP,nameNodePort);
    }

    /**
     * 本线程建立与NameNode的连接，并启动心跳线程
     * @param nameNodeIP nameNodeIP
     * @param nameNodePort nameNodePort
     */
    public void connect(String nameNodeIP,String nameNodePort){
        this.nameNodeIP = nameNodeIP;
        this.nameNodePort = nameNodePort;

        Connection instance = this;// 把本身传递给WatchHandler，便于更新 sortedServerMap

        group = new NioEventLoopGroup();
        Bootstrap b = new Bootstrap();
        b.group(group)
            .channel(NioSocketChannel.class)
            .remoteAddress(new InetSocketAddress(nameNodeIP, Integer.parseInt(nameNodePort)))
            .handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch)  throws Exception {
                    ChannelPipeline p = ch.pipeline();
                    p.addLast(new ObjectDecoder(2048, ClassResolvers.cacheDisabled(this.getClass().getClassLoader())));// in
                    p.addLast(new ObjectEncoder());// out
                    p.addLast(new WatchHandler(instance));// in
                }
            });

//      ChannelFuture f = b.connect().sync();// 阻塞等待连接建立成功
        ChannelFuture f = b.connect();// sync连不上会直接抛出异常，要用listener才能实行重连
        f.addListener((ChannelFutureListener) channelFuture -> {
            if (channelFuture.isSuccess()){// 连接成功
                nameNodeChannel = f.channel();
                nameNodeChannel.writeAndFlush(new WatchRequest(true));// 立即请求获得hash 环

                // 启动心跳线程
                running = true;
                heartbeat = new Thread(new HeartBeat(),"heartbeat");
                heartbeat.setDaemon(true);// 心跳是个daemon线程，主线程结束心跳线程自然也要结束
                heartbeat.start();
                logger.debug("nameNode connection established and Heartbeat started");

                // 监听连接关闭事件，可能是主动关闭，也可能是被动关闭，所以要注意释放资源
                ChannelFuture f1 = nameNodeChannel.closeFuture();
                f1.addListener((ChannelFutureListener) cf -> {
                    logger.info("nameNode Connection closed");
                    disconnectPassive();
                    disconnectHA();
                });

            }else{// 首次连接失败，重连
                logger.info("nameNode Connection failed, retrying,{},{}",nameNodeIP,nameNodePort);
                Thread.sleep(3000);
                group.shutdownGracefully();//回收资源，因为下一次递归调用自己会分配
                connect(nameNodeIP,nameNodePort);
            }
        });

        if (useHA){
            if (party==null){
                logger.debug("initializing third party");
                party = new ZKThirdParty("127.0.0.1:2181,127.0.0.1:3181,127.0.0.1:4181",2000,8000,"/CHKV/masterNode",1000,10);
            }
            clientHA(party);
        }

        // 阻塞至 有 dataNode 可用，保证connect返回成功的含义，没有DataNode，connect就一直忙等待
        while (sortedServerMap.isEmpty()){}
    }

    /**
     * 断开与NameNode的连接，并中断心跳线程，关闭与DataNode的连接
     */
    public void disconnect(){
        logger.debug("主动关闭本连接");// 主动关闭需要手动释放资源并把HA置为null
        // 释放高可用
        if (useHA && party != null) {
            party.releaseCon();
            party = null;
        }
        disconnectPassive();
        // 关闭NameNode连接
        nameNodeChannel.close();

    }

    public void disconnectPassive(){
        logger.debug("被动关闭本连接");// 被动关闭清理资源
        // 停止心跳
        running = false;heartbeat.interrupt();// interrupt能立即起效
        // 关闭所有DataNode连接
        dataNodeChannelList.forEach((IPPort, channel)-> {
            if (channel.isOpen()){
//                logger.debug("dataNode {} connection isOpen",IPPort);
                channel.close();
            }
            else {
//                logger.debug("dataNode {} connection already closed",IPPort);
            }
            logger.debug("dataNode {} connection closed",IPPort);
        });
        // 必须shutdown
        group.shutdownGracefully();

    }

    public void disconnectHA(){
        if (useHA && party!=null){
            while (nameNodeChannel == null || !nameNodeChannel.isActive()){
                try {
                    logger.error("waiting for master info from registry");
                    Thread.sleep(3000);// 断开连接后新的节点不一定马上争取master成功，所以等待一段时间
                } catch (InterruptedException e) {
                    logger.error("waiting master exception：{}",e.getMessage());
                }
            }
            logger.info("get masterNode,go on");
            // 等待注册中心通知重连，这里不重连只等待
//            String s = party.getInstantMaster();
//            HAHelper helper = new HAHelper(s);
//            logger.info("present connection closed, present NameNode master:{},retrying",s);
//            connect(helper.getClientIP(),helper.getClientPort());
        }
    }

    /**
     * 连接DataNode，如果连接存在则复用
     * @param dataNodeIP dataNodeIP
     * @param dataNodePort dataNodePort
     */
    private Channel getDataNodeConnection(String dataNodeIP, String dataNodePort){
        String key = dataNodeIP+":"+dataNodePort;
        Channel dataNodeChannel = dataNodeChannelList.get(key);
        if (dataNodeChannel != null && dataNodeChannel.isActive()){// 之前建立过连接并且连接依然活跃
            return dataNodeChannel;
        }

        // 需要新建DataNode连接
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap b = new Bootstrap();
        b.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.RCVBUF_ALLOCATOR,new FixedRecvByteBufAllocator(4096))// 最大收包长度设置为4096 byte
                .remoteAddress(new InetSocketAddress(dataNodeIP, Integer.parseInt(dataNodePort)))
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch)  throws Exception {
                        ChannelPipeline p = ch.pipeline();
                        p.addLast(new DataHandler(dataResponseMap));// in
                    }
                });
        try {
            ChannelFuture f = b.connect().sync();// 同步发起连接,阻塞等待
            dataNodeChannel = f.channel();
            dataNodeChannelList.put(key,dataNodeChannel);
            logger.debug("DataNode {} connection established",key);
            ChannelFuture f1 = dataNodeChannel.closeFuture();
            f1.addListener((ChannelFutureListener) channelFuture -> {
                logger.debug("DataNode Connection connection closed");
                group.shutdownGracefully();
            });// 异步回调，避免阻塞
        } catch (InterruptedException e) {
            logger.debug("DataNode {} connection interrupt",key,e);
            dataNodeChannel = null;
        }
        return dataNodeChannel;
    }

    /**
     * 计算DataNode节点并发送命令
     */
    DataResponse sendCommand(DataRequest request){ // 默认就是 package-private
        Optional<DataResponse> response = Optional.empty();

        if (sortedServerMap.isEmpty()) {
            logger.error("no available DataNode");
            return noDataNode;
        }
//        logger.debug("Hash Circle",sortedServerMap);

        String IPPort = getServer(sortedServerMap,request.getKey(),false);
        String[] strings = IPPort.split(":");
        String IP = strings[0];String port = strings[1];

        List<DataRequest> list = new LinkedList<>();
        list.add(request);
        ByteBuf buf = Encoder.dataRequestToBytes(list);

        Channel channel = getDataNodeConnection(IP,port);
        if (channel != null){
            channel.writeAndFlush(buf);
            logger.debug("client 向 {} 请求 {}",IPPort,request);
            while (dataResponseMap.get(request.getID())==null){}// 阻塞至有结果
            response = Optional.ofNullable(dataResponseMap.get(request.getID()));// 设置响应
            dataResponseMap.remove(request.getID());//消除结果，省空间
        }
        return response.orElse(noDataNode);
    }

    @Override
    public void close() throws Exception {// 与上面的含参构造函数搭配 就能使用 try-with-source
        disconnect();
    }

    class HeartBeat implements Runnable{
        @Override
        public void run() {
            while (running){// 没有被中断就持续发送心跳，中断就结束心跳线程
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    logger.debug("nameNode heartbeat InterruptedException,{}",e.getMessage());
                    break;
                }
                if (nameNodeChannel!=null && nameNodeChannel.isActive()){
                    nameNodeChannel.writeAndFlush(watchRequest);
                    logger.debug("send nameNode heartbeat:{},local hash ring :{}",watchRequest,sortedServerMap);
                }else {// 心跳失败 重连
                    logger.debug("nameNode heartbeat lost, retrying");
                    connect(nameNodeIP,nameNodePort);
                    break;// 重连成功后自然结束本线程，也就是说与断开的连接相关联的心跳线程结束
                }
            }
        }
    }

    private void clientHA(HAThirdParty party ){

        // 下面代码与具体HA实现无关，可复用
        party.getInstantMaster();
        Consumer<String> consumer = s -> {
            if (s==null){
                logger.error("masterNode is down, waiting");
            }else{
                logger.info("masterNode may have changed:{}",s);
                HAHelper helper = new HAHelper(s);
                String nameNodeIP = helper.getClientIP();
                String nameNodePort = helper.getClientPort();
                if (!(nameNodeIP.equals(this.nameNodeIP)&&nameNodePort.equals(this.nameNodePort))){// 不同，那肯定需要重连
                    logger.info("masterNode indeed have changed,reconnecting");
                    disconnect();
                    connect(nameNodeIP,nameNodePort);
                }else {// 相同，有可能断线后又上线
                    if (!nameNodeChannel.isActive()){//断线了就重连
                        disconnect();
                        connect(nameNodeIP,nameNodePort);
                    }
                    // 否则就没断线，只是nameNode和高可用注册中心连接抖动
                }
            }
        };

        party.beginWatch(consumer);
        while (party.getMasterNode()==null);//忙等待
        logger.debug("present master NameNode:{}",party.getMasterNode());

    }
}
