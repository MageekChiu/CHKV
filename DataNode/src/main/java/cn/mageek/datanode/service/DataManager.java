package cn.mageek.datanode.service;

import cn.mageek.datanode.handler.*;
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

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import static cn.mageek.common.util.PropertyLoader.load;

/**
 * 管理来自client或者其他DataNode的data请求,保持连接
 * @author Mageek Chiu
 * @date 2018/5/5 0007:20:18
 */
public class DataManager implements Runnable{
    private static final Logger logger = LoggerFactory.getLogger(DataManager.class);
    private static String clientPort;
    private static final Map<String,Channel> channelMap = new ConcurrentHashMap<>();//管理所有客户端连接

    private CountDownLatch countDownLatch;

    static {
        try( InputStream in = ClassLoader.class.getResourceAsStream("/app.properties")) {
            Properties pop = new Properties(); pop.load(in);
            clientPort = load(pop,"datanode.client.port"); //dataNode对client开放的端口
            logger.debug("config clientPort:{}", clientPort);
        } catch (IOException e) {
            logger.error("read config error",e);
        }
    }

    public DataManager(CountDownLatch countDownLatch) {
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
                    .option(ChannelOption.SO_BACKLOG, 512)//最大等待连接
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
            businessGroup.shutdownGracefully();
        }

    }




}




