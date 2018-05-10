package cn.mageek.client.job;

import cn.mageek.client.handler.DataHandler;
import cn.mageek.client.handler.WatchHandler;
import cn.mageek.common.model.DataRequest;
import cn.mageek.common.model.WatchRequest;
import io.netty.bootstrap.Bootstrap;
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
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Client直接发送命令即可，由Connection保存有效DataNode并负责计算具体的DataNode然后发起连接与请求，
 * @author Mageek Chiu
 * @date 2018/5/10 0010:19:33
 */
public class Connection {

    private static final Logger logger = LoggerFactory.getLogger(Connection.class);
    private static WatchRequest watchRequest = new WatchRequest(false);// 心跳是不需要立即回复的

    private Channel nameNodeChannel;// NameNode Channel
    private Channel dataNodeChannel;// 当前 DataNode Channel
    public ConcurrentSkipListMap<Integer, String> sortedServerMap =  new ConcurrentSkipListMap<>();// DataNode hash 环
//    private Map<String,Channel> dataNodeList = new ConcurrentHashMap<>();// ip:port -> channel // 不保存连接，发完就断，但是将来可以考虑连接池
    private Thread heartbeat;// NameNode心跳线程


    /**
     * 本线程建立与NameNode的连接，并启动心跳线程
     * @param nameNodeIP nameNodeIP
     * @param nameNodePort nameNodePort
     */
    public void connect(String nameNodeIP,String nameNodePort){
        Connection instance = this;// 把本身传递给WatchHandler
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap b = new Bootstrap();
        b.group(group)
            .channel(NioSocketChannel.class)
            .remoteAddress(new InetSocketAddress(nameNodeIP, Integer.parseInt(nameNodePort)))
            .handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch)  throws Exception {
                    ChannelPipeline p = ch.pipeline();
                    p.addLast(new ObjectDecoder(2048, ClassResolvers.cacheDisabled(this.getClass().getClassLoader())));
                    p.addLast(new ObjectEncoder());// out
                    p.addLast(new WatchHandler(instance));// in
                }
            });
        try {
            ChannelFuture f = b.connect().sync();
            nameNodeChannel = f.channel();
            nameNodeChannel.writeAndFlush(new WatchRequest(true));// 立即请求获得hash 环
            heartbeat = new Thread(() -> {
                while (!Thread.interrupted()){
                    nameNodeChannel.writeAndFlush(watchRequest);
                    logger.debug("send heartbeat:{},hash ring :{}",watchRequest,sortedServerMap);
                    try {
                        Thread.sleep(20000);
                    } catch (InterruptedException e) {
                        logger.debug("nameNode heartbeat InterruptedException",e);
                    }
                }
            });
            heartbeat.start();
            logger.debug("nameNode connection established and Heartbeat started");
        } catch (InterruptedException e) {
            logger.debug("nameNode connection InterruptedException",e);
            heartbeat.interrupt();
        }
    }

    /**
     * 断开与NameNode的连接，并中断心跳线程
     */
    public void disconnect(){
        try {
            heartbeat.interrupt();
            nameNodeChannel.closeFuture().sync();
            logger.debug("nameNode connection closed");
        } catch (InterruptedException e) {
            logger.debug("nameNode connection interrupt",e);
        }
    }


    /**
     * 连接DataNode
     * @param dataNodeIP dataNodeIP
     * @param dataNodePort dataNodePort
     */
    private void connectDataNode(String dataNodeIP,String dataNodePort){
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap b = new Bootstrap();
        b.group(group)
            .channel(NioSocketChannel.class)
            .remoteAddress(new InetSocketAddress(dataNodeIP, Integer.parseInt(dataNodePort)))
            .handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch)  throws Exception {
                    ChannelPipeline p = ch.pipeline();
                    p.addLast(new ObjectDecoder(2048, ClassResolvers.cacheDisabled(this.getClass().getClassLoader())));// in 进制缓存类加载器
                    p.addLast(new ObjectEncoder());// out
                    p.addLast(new DataHandler());// in
                }
            });
        try {
            ChannelFuture f = b.connect().sync();// 发起连接,阻塞等待
            logger.debug("connectDataNode connection established");
            dataNodeChannel = f.channel();
            dataNodeChannel.closeFuture().sync();// 这是一段阻塞的代码，除非链路断了，否则是不会停止阻塞的，我们可以在handler中手动关闭，达到关闭客户端的效果
            logger.debug("connectDataNode connection closed");
        } catch (InterruptedException e) {
            logger.debug("connectDataNode connection interrupt",e);
        }
    }


    /**
     *
     * @param CMD 命令
     * @param args 潜在的key,value
     */
    public void sendCommand(String CMD,String... args){
        DataRequest request ;
//        dataNodeChannel.writeAndFlush(request);
    }

}
