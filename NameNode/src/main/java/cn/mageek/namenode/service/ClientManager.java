package cn.mageek.namenode.service;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * 管理所有client
 * @author Mageek Chiu
 * @date 2018/5/7 0007:20:18
 */
public class ClientManager implements Runnable{
    private static final Logger logger = LoggerFactory.getLogger(ClientManager.class);
    private final String port;
    private static final Map<String,Channel> channelMap = new ConcurrentHashMap<>();//管理所有连接

    private CountDownLatch countDownLatch;


    public ClientManager(String port, CountDownLatch countDownLatch) {
        this.port = port;
        this.countDownLatch = countDownLatch;
    }

    public void run() {
        // Configure the server.
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);//接收连接
        EventLoopGroup workerGroup = new NioEventLoopGroup(4);//处理连接的I/O事件
        EventExecutorGroup businessGroup = new DefaultEventExecutorGroup(8);//处理耗时业务逻辑，我实际上为了统一起见把全部业务逻辑都放这里面了
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)//新建一个channel
                    .option(ChannelOption.SO_BACKLOG, 512)//最大等待连接
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            // out 必须放在最后一个 in 前面，也就是必须是以 in 结尾。逻辑是in 顺序执行完毕以后从 pipeline 反向查找 out
                            ChannelPipeline p = ch.pipeline();
                            // out 执行顺序为注册顺序的逆序
                            // in 执行顺序为注册顺序
                            p.addLast("ReadTimeoutHandler",new ReadTimeoutHandler(600));// in // 多少秒超时
//                            p.addLast("OtherHandler",new OtherHandler());// in  // 纯粹是为了占位，把PushMsgHandler防止BusinessHandler下面。注释掉也没事，in 结尾 是扯淡，看源码，netty会自己找第一个

                        }
                    });

            // Start the server. 采用同步等待的方式
            ChannelFuture f = b.bind(Integer.parseInt(port)).sync();
            logger.info("ClientManager is up now and listens on {}", f.channel().localAddress());
            countDownLatch.countDown();

            // Wait until the server socket is closed.
            f.channel().closeFuture().sync();
            logger.info("ClientManager is down");

        } catch (InterruptedException e) {
            logger.error("ClientManager start error: ", e);
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
            businessGroup.shutdownGracefully();
        }

    }




}




