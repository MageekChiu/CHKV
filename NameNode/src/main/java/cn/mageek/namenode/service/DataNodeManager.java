package cn.mageek.namenode.service;

import cn.mageek.namenode.handler.DataNodeHeartBeatHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static cn.mageek.common.util.PropertyLoader.load;
import static cn.mageek.common.util.PropertyLoader.loadWorkThread;
import static cn.mageek.namenode.main.NameNode.countDownLatch;
import static cn.mageek.namenode.main.NameNode.dataNodePort;
import static cn.mageek.namenode.main.NameNode.dataNodeThread;

/**
 * 管理所有dataNode
 * @author Mageek Chiu
 * @date 2018/5/7 0007:20:18
 */
public class DataNodeManager implements Runnable{
    private static final Logger logger = LoggerFactory.getLogger(DataNodeManager.class);

//    private static String dataNodePort;
//    private static int dataNodeThread;

//    private Map<String,Channel> dataNodeMap;//管理所有datanode
//    private Map<String,String> dataNodeClientMap ;//管理所有datanode 对client开放的IP与端口
//    private ConcurrentSkipListMap<Integer, String> sortedServerMap ;//管理所有datanode 对应 hash 和 ip:port

//    private CountDownLatch countDownLatch;//

//    static {
//        try( InputStream in = ClassLoader.class.getResourceAsStream("/app.properties")) {
//            Properties pop = new Properties();
//            pop.load(in);
//            dataNodePort = load(pop,"namenode.datanode.port");// 对dataNode开放的端口
//            dataNodeThread = loadWorkThread(pop,"namenode.datanode.workThread");
//            logger.debug("config dataNodePort:{},dataNodeThread:{}", dataNodePort,dataNodeThread);
//        } catch (IOException e) {
//            logger.error("read config error",e);
//        }
//    }

//    public DataNodeManager(Map<String,Channel> dataNodeMap, Map<String,String> dataNodeClientMap , ConcurrentSkipListMap<Integer, String> sortedServerMap, CountDownLatch countDownLatch) {
//        this.dataNodeMap = dataNodeMap;
//        this.dataNodeClientMap = dataNodeClientMap;
//        this.countDownLatch = countDownLatch;
//        this.sortedServerMap = sortedServerMap;
//    }

//    public DataNodeManager(CountDownLatch countDownLatch) {
//        this.countDownLatch = countDownLatch;
//    }

    public void run() {
        // Configure the server.
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);//接收连接
        EventLoopGroup workerGroup = new NioEventLoopGroup(dataNodeThread);//处理连接的I/O事件
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)//新建一个channel
                    .option(ChannelOption.SO_BACKLOG, 64)//最大等待连接
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            p.addLast("ReadTimeoutHandler",new ReadTimeoutHandler(31));// in // 多少秒超时
                            p.addLast(new ObjectDecoder(2048, ClassResolvers.cacheDisabled(this.getClass().getClassLoader())));// in 禁止缓存类加载器
                            p.addLast(new ObjectEncoder());// out
//                            p.addLast(new DataNodeHeartBeatHandler(dataNodeMap,dataNodeClientMap,sortedServerMap));// in
                            p.addLast(new DataNodeHeartBeatHandler());// in
                        }
                    });

            // Start the server. 采用同步等待的方式
            ChannelFuture f = b.bind(Integer.parseInt(dataNodePort)).sync();
            logger.info("DataNodeManager is up now and listens on {}", f.channel().localAddress());
            countDownLatch.countDown();

            // Wait until the server socket is closed.
            f.channel().closeFuture().sync();
            logger.info("DataNodeManager is down");

        } catch (InterruptedException e) {
            logger.error("DataNodeManager start error: ", e);
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }

    }




}




