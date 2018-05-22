package cn.mageek.datanode.service;

import cn.mageek.datanode.handler.*;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import static cn.mageek.common.util.PropertyLoader.loadWorkThread;
import static cn.mageek.datanode.main.DataNode.clientPort;
import static cn.mageek.datanode.main.DataNode.countDownLatch;

/**
 * 管理来自client或者其他DataNode的data请求,保持连接
 * @author Mageek Chiu
 * @date 2018/5/5 0007:20:18
 */
public class DataManager implements Runnable{
    private static final Logger logger = LoggerFactory.getLogger(DataManager.class);
    private static int workThread ;

    public static final Map<String,Channel> clientMap = new ConcurrentHashMap<>();//管理所有客户端连接

    static {
        try( InputStream in = ClassLoader.class.getResourceAsStream("/app.properties")) {
            Properties pop = new Properties(); pop.load(in);
            workThread = loadWorkThread(pop,"datanode.workThread"); // IO线程
            logger.debug("config clientPort:{}，workThread:{}", clientPort,workThread);
        } catch (IOException e) {
            logger.error("read config error",e);
        }
    }

    public void run() {

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);//接收连接
        EventLoopGroup workerGroup = new NioEventLoopGroup(workThread);//处理连接的I/O事件

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class) //   新建一个channel
                    .option(ChannelOption.SO_BACKLOG, 512)//最大等待连接
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch){
                            ChannelPipeline p = ch.pipeline();
                            p.addLast("ReadTimeoutHandler",new ReadTimeoutHandler(100));// in // 多少秒超时
                            p.addLast("SendMsgHandler",new SendMsgHandler());// out //发送消息编码
                            p.addLast("ClientHandler",new ClientHandler());// in //连接管理
                            // 下面这两都不管用，尚不清楚原因
//                            p.addLast("DelimiterBasedFrameDecoder",new DelimiterBasedFrameDecoder(2048,true,true,Unpooled.copiedBuffer("\r\n".getBytes())));// in //基于分隔符的协议解码
//                            p.addLast("StringDecoder",new StringDecoder());// in // 字符串解码器
//                            p.addLast("LineBasedFrameDecoder",new LineBasedFrameDecoder(1024,true,true));// in //基于行的协议解码
//                            p.addLast("StringDecoder",new StringDecoder());// in // 字符串解码器
                            p.addLast("RcvMsgHandler",new RcvMsgHandler());// in //将行数据解码为消息对象
                            p.addLast("BusinessHandler",new BusinessHandler());// in //解析业务数据，没有特别耗时的操作，还是不要切换线程

                        }
                    });

            // Start the server. 采用同步等待的方式
            ChannelFuture f = b.bind(Integer.parseInt(clientPort)).sync();
            logger.info("DataManager is up now and listens on {}", f.channel().localAddress());
            countDownLatch.countDown();

            // Wait until the server socket is closed.
            f.channel().closeFuture().sync();
            logger.info("DataManager is down");

        } catch (InterruptedException e) {
            logger.error("DataManager start error: ", e);
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }

    }




}




