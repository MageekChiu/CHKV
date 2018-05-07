package cn.mageek.datanode.cron;

import cn.mageek.datanode.handler.HeartBeatHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.Properties;

import static cn.mageek.datanode.res.ConstPool.L_SUCCESS;


/**
 * 向namenode发送心跳
 * @author Mageek Chiu
 * @date 2018/5/7 0007:10:44
 */
public class Heartbeat implements Runnable{
    private static final Logger logger = LoggerFactory.getLogger(Heartbeat.class);

    private static String nameNodeIP;
    private static int nameNodePort;
    private static String clientPort;
    private static String clientIP;

    private static Bootstrap b;
    private static EventLoopGroup group;

    static { // 读取配置
        try (InputStream in = ClassLoader.class.getResourceAsStream("/app.properties")) {
            Properties pop = new Properties();
            pop.load(in);
            nameNodeIP = pop.getProperty("datanode.namenode.ip");// nameNode 对DataNode开放心跳IP
            nameNodePort = Integer.parseInt(pop.getProperty("datanode.namenode.port"));// nameNode 对DataNode开放心跳Port
            clientPort = pop.getProperty("datanode.client.port");// dataNode对client开放的端口
            clientIP = pop.getProperty("datanode.client.ip");// dataNode对client开放的ip
            logger.debug("config nameNodeIP:{},nameNodePort:{}", nameNodeIP, nameNodePort);
        } catch (IOException e) {
            logger.error("read config error",e);
        }
        group = new NioEventLoopGroup();
        b = new Bootstrap();
        b.group(group)
            .channel(NioSocketChannel.class)
            .remoteAddress(new InetSocketAddress(nameNodeIP, nameNodePort))// 配置namenode ip port
            .handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch)  throws Exception {
                    ch.pipeline().addLast(new ObjectDecoder(2048, ClassResolvers.cacheDisabled(this.getClass().getClassLoader())));// in 进制缓存类加载器
                    ch.pipeline().addLast(new ObjectEncoder());// out
                    ch.pipeline().addLast(new HeartBeatHandler(clientIP,clientPort));// in
                }
            });

    }

    public void run(){
        try {
            ChannelFuture f = b.connect().sync();// 发起连接
            f.channel().closeFuture().sync();// 这是一段阻塞的代码，除非链路断了，否则是不会终止的，我们可以在handler中手动关闭，达到关闭客户端的效果
            logger.debug(L_SUCCESS+",心跳连接关闭");
//            group.shutdownGracefully().sync();
        } catch (InterruptedException e) {
            logger.error("InterruptedException",e);
        }
    }
}
