package cn.mageek.datanode.service;

import cn.mageek.datanode.handler.*;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * 监听和接受连接请求，亦即创建channel并配置消息处理的handler
 * @author Mageek Chiu
 * @date 2018/5/5 0007:20:18
 */
public class ClientManager implements Runnable{
    private static final Logger logger = LoggerFactory.getLogger(ClientManager.class);
    private final String port;
    private static final Map<String,Channel> channelMap = new ConcurrentHashMap<>();//管理所有客户端连接

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
                    .channel(NioServerSocketChannel.class) //   新建一个channel
                    .option(ChannelOption.SO_BACKLOG, 64)//最大等待连接
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            // out 执行顺序为注册顺序的逆序
                            // in 执行顺序为注册顺序
                            p.addLast("ReadTimeoutHandler",new ReadTimeoutHandler(100));// in // 多少秒超时
                            p.addLast("SendMsgHandler",new SendMsgHandler());// out //发送消息编码
                            p.addLast("ClientHandler",new ClientHandler(channelMap));// in //连接管理
                            // 下面这两都不管用，尚不清楚原因
//                            p.addLast("DelimiterBasedFrameDecoder",new DelimiterBasedFrameDecoder(2048,true,true,Unpooled.copiedBuffer("\r\n".getBytes())));// in //基于分隔符的协议解码
//                            p.addLast("StringDecoder",new StringDecoder());// in // 字符串解码器
//                            p.addLast("LineBasedFrameDecoder",new LineBasedFrameDecoder(1024,true,true));// in //基于行的协议解码
//                            p.addLast("StringDecoder",new StringDecoder());// in // 字符串解码器
                            p.addLast("RcvMsgHandler",new RcvMsgHandler());// in //将行数据解码为消息对象
                            p.addLast(businessGroup,"BusinessHandler",new BusinessHandler());// in //解析业务数据
                            p.addLast(businessGroup,"PushMsgHandler",new PushMsgHandler());// out //合成推送消息
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




