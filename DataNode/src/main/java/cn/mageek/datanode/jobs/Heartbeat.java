package cn.mageek.datanode.jobs;

import cn.mageek.common.model.HeartbeatRequest;
import cn.mageek.datanode.handler.HeartBeatHandler;
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
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Properties;

import static cn.mageek.common.model.HeartbeatType.*;
import static cn.mageek.common.util.PropertyLoader.load;


/**
 * 向namenode发送心跳
 * @author Mageek Chiu
 * @date 2018/5/7 0007:10:44
 */
public class Heartbeat extends DataRunnable{
    private static final Logger logger = LoggerFactory.getLogger(Heartbeat.class);

    private static String nameNodeIP;
    private static int nameNodePort;
    private static String clientPort;
    private static String clientIP;


    private static ChannelFuture f;

    static { // 读取配置

//        JVM自定义参数通过java命令的可选项:
//        -D<name>=<value>     如 java -Ddatanode.client.ip=192.168.0.136 -Ddatanode.client.port=10099 DataNode
//                来传入JVM，传入的参数作为system的property。因此在程序中可以通过下面的语句获取参数值：
//        System.getProperty(<name>)

        try (InputStream in = ClassLoader.class.getResourceAsStream("/app.properties")) {
            Properties pop = new Properties();
            pop.load(in);
//            nameNodeIP = System.getProperty("datanode.namenode.ip");// 首先获得环境变量，若空则读取配置文件中的默认值
//            nameNodeIP = (nameNodeIP == null ? pop.getProperty("datanode.namenode.ip") : nameNodeIP);// nameNode 对DataNode开放心跳IP
            nameNodeIP = load(pop,"datanode.namenode.ip");
//            nameNodePort = Integer.parseInt(System.getProperty("datanode.namenode.port") == null ? pop.getProperty("datanode.namenode.port") : System.getProperty("datanode.namenode.port"));// nameNode 对DataNode开放心跳Port
            nameNodePort = Integer.parseInt(load(pop,"datanode.namenode.port"));
//            clientIP = System.getProperty("datanode.client.ip");// dataNode对client开放的ip
//            clientIP = (clientIP == null ? pop.getProperty("datanode.client.ip") : clientIP);//
            clientIP = load(pop,"datanode.client.ip");
//            clientPort = System.getProperty("datanode.client.port");//dataNode对client开放的端口
//            clientPort = (clientPort == null ? pop.getProperty("datanode.client.port") : clientPort);//
            clientPort = load(pop,"datanode.client.port");
            logger.debug("Heartbeat config nameNodeIP:{},nameNodePort:{},clientIP:{},clientPort:{}", nameNodeIP, nameNodePort,clientIP,clientPort);
        } catch (IOException e) {
            logger.error("Heartbeat config error",e);
        }

    }
    @Override
    public void connect(Map<String,String> dataPool){
        this.DATA_POOL = dataPool;
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap b = new Bootstrap();
        b.group(group)
                .channel(NioSocketChannel.class)
                .remoteAddress(new InetSocketAddress(nameNodeIP, nameNodePort))// 配置namenode ip port
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch)  throws Exception {
                        ChannelPipeline p = ch.pipeline();
                        p.addLast(new ObjectDecoder(2048, ClassResolvers.cacheDisabled(this.getClass().getClassLoader())));// in 进制缓存类加载器
                        p.addLast(new ObjectEncoder());// out
                        p.addLast("HeartBeatHandler",new HeartBeatHandler(clientIP,clientPort,DATA_POOL));// in
                    }
                });
        try {
            f = b.connect().sync();// 发起连接,阻塞等待
            logger.debug("Heartbeat connection established");
            run1(ONLINE);// 成功后就发起上线请求

//            f.channel().closeFuture().sync();// 这是一段阻塞的代码，除非链路断了，否则是不会停止阻塞的，我们可以在handler中手动关闭，达到关闭客户端的效果
            ChannelFuture future = f.channel().closeFuture();// 采用异步加回调函数的做法，防止阻塞
            future.addListener((ChannelFutureListener) channelFuture -> {// 关闭成功
                group.shutdownGracefully().sync();
                logger.debug("Heartbeat connection closed");
            });
        } catch (InterruptedException e) {
            logger.error("Heartbeat connection InterruptedException",e);
        }
    }

    @Override
    public void run(){
        run1(RUNNING);// 发送心跳
    }

    /**
     * 发送不同状态的心跳
     * @param status 状态
     */
    private void run1(String status){
        long memoryAvailable = Runtime.getRuntime().freeMemory();
        HeartbeatRequest request = new HeartbeatRequest(clientIP+":"+clientPort,status,memoryAvailable);
        logger.debug("DataNode sent: " + request);
        f.channel().writeAndFlush(request);
    }
}
