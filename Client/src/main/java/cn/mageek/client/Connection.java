package cn.mageek.client;

import cn.mageek.client.handler.DataHandler;
import cn.mageek.client.handler.WatchHandler;
import cn.mageek.common.model.DataRequest;
import cn.mageek.common.model.DataResponse;
import cn.mageek.common.model.WatchRequest;
import cn.mageek.common.util.Encoder;
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
    private static final DataResponse noDataNode = new DataResponse(SINGLE_ERROR,"no DataNode available");

    public ConcurrentSkipListMap<Integer, String> sortedServerMap =  new ConcurrentSkipListMap<>();// DataNode hash 环

    private Channel nameNodeChannel;// NameNode Channel
    private Map<String,Channel> dataNodeChannelList = new ConcurrentHashMap<>();// ip:port -> channel // 保存DataNode连接
    private Thread heartbeat;// NameNode心跳线程
    private volatile boolean running = true;
    private volatile Map<String ,DataResponse> dataResponseMap = new ConcurrentHashMap<>(1024);// 存放所有响应

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

    /**
     * 本线程建立与NameNode的连接，并启动心跳线程
     * @param nameNodeIP nameNodeIP
     * @param nameNodePort nameNodePort
     */
    public void connect(String nameNodeIP,String nameNodePort){
        Connection instance = this;// 把本身传递给WatchHandler，便于更新 sortedServerMap
        EventLoopGroup group = new NioEventLoopGroup();
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
        try {
            ChannelFuture f = b.connect().sync();// 阻塞等待连接建立成功
            nameNodeChannel = f.channel();
            nameNodeChannel.writeAndFlush(new WatchRequest(true));// 立即请求获得hash 环
            heartbeat = new Thread(() -> {
                while (running){// 没有被中断就持续发送心跳，中断就结束心跳线程
                    try {
                        Thread.sleep(20000);
                    } catch (InterruptedException e) {
                        logger.debug("nameNode heartbeat InterruptedException,{}",e.getMessage());
                    }
                    nameNodeChannel.writeAndFlush(watchRequest);
                    logger.debug("send nameNode heartbeat:{},hash ring :{}",watchRequest,sortedServerMap);
                }
            },"heartbeat");
            heartbeat.start();
            logger.debug("nameNode connection established and Heartbeat started");
            ChannelFuture f1 = nameNodeChannel.closeFuture();
            f1.addListener((ChannelFutureListener) channelFuture -> {// 监听连接关闭事件
                group.shutdownGracefully();// 必须shutdown
                logger.info("nameNode Connection connection closed");
            });// 异步回调，避免阻塞
            while (sortedServerMap.isEmpty()){}// 阻塞至 有 dataNode 可用，保证connect返回成功的含义，没有DataNode，connect就一直忙等待
        } catch (InterruptedException e) {
            logger.debug("nameNode connection InterruptedException",e);
            running = false;
        }
    }

    /**
     * 断开与NameNode的连接，并中断心跳线程，关闭与DataNode的连接
     */
    public void disconnect(){
        logger.debug("关闭连接");
        running = false;heartbeat.interrupt();// 停止心跳
        dataNodeChannelList.forEach((IPPort, channel)-> {
            if (channel.isOpen()){
//                logger.debug("dataNode {} connection isOpen",IPPort);
                channel.close();
            }
            else {
//                logger.debug("dataNode {} connection already closed",IPPort);
            }
            logger.debug("dataNode {} connection closed",IPPort);
        }); // 关闭所有DataNode连接
        nameNodeChannel.close(); // 关闭NameNode连接
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
}
