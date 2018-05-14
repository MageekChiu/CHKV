package cn.mageek.datanode.job;

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
import java.util.concurrent.TimeUnit;

import static cn.mageek.common.model.HeartbeatType.*;
import static cn.mageek.common.util.PropertyLoader.load;


/**
 * 向namenode发送心跳
 * @author Mageek Chiu
 * @date 2018/5/7 0007:10:44
 */
public class Heartbeat extends DataRunnable{
//public class Heartbeat implements Runnable{

    private static final Logger logger = LoggerFactory.getLogger(Heartbeat.class);

    private static String nameNodeIP = "127.0.0.1";// 硬编码默认值，可以被环境变量或者配置文件覆盖
    private static int nameNodePort = 10101;
    private static String clientPort;
    private static String clientIP;

    private static EventLoopGroup group = new NioEventLoopGroup();
    private static Channel nameNodeChannel = null;

    static { // 初始化

//        JVM自定义参数通过java命令的可选项:
//        -D<name>=<value>     如 java -Ddatanode.client.ip=192.168.0.136 -Ddatanode.client.port=10099 DataNode
//                来传入JVM，传入的参数作为system的property。因此在程序中可以通过下面的语句获取参数值：
//        System.getProperty(<name>)

        try (InputStream in = ClassLoader.class.getResourceAsStream("/app.properties")) {
            // 读取配置
            Properties pop = new Properties();
            pop.load(in);
            nameNodeIP = load(pop,"datanode.namenode.ip");// nameNode 对DataNode开放心跳IP
            nameNodePort = Integer.parseInt(load(pop,"datanode.namenode.port"));// nameNode 对DataNode开放心跳Port
            clientIP = load(pop,"datanode.client.ip");//dataNode对client开放的ip
            clientPort = load(pop,"datanode.client.port");//dataNode对client开放的端口
            logger.debug("Heartbeat config nameNodeIP:{},nameNodePort:{},clientIP:{},clientPort:{}", nameNodeIP, nameNodePort,clientIP,clientPort);



        } catch (IOException e) {
            logger.error("Heartbeat config error",e);
        }
    }

    // 实际连接
    @Override
    public void connect(){
        if (nameNodeChannel != null && nameNodeChannel.isActive()) return;
        // 这里必须new 否则可能就会失败多次被回收了
        group = new NioEventLoopGroup();
        Bootstrap b = new Bootstrap();
        // BootStrap 初始化
        b.group(group)
                .channel(NioSocketChannel.class)
//                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,5000)// 设置超时
                .remoteAddress(new InetSocketAddress(nameNodeIP, nameNodePort))// 配置namenode ip port
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch)  throws Exception {
                        ChannelPipeline p = ch.pipeline();
                        p.addLast(new ObjectDecoder(2048, ClassResolvers.cacheDisabled(this.getClass().getClassLoader())));// in 进制缓存类加载器
                        p.addLast(new ObjectEncoder());// out
                        p.addLast("HeartBeatHandler",new HeartBeatHandler());// in
                    }
                });

        ChannelFuture f= b.connect();
        f.addListener(new  ConnectionListener());// 连接监听器
        ChannelFuture future = f.channel().closeFuture();// 采用异步加回调函数的做法，防止阻塞
        future.addListener((ChannelFutureListener) channelFuture -> {// 关闭成功
            group.shutdownGracefully().sync();
            logger.debug("Heartbeat connection closed");
        });
    }

    @Override
    public void run(){
        run1(RUNNING);// 运行中发送心跳
    }

    /**
     * 发送不同状态的心跳
     * @param status 状态
     */
    public void run1(String status){
        long memoryAvailable = Runtime.getRuntime().freeMemory();
        HeartbeatRequest request = new HeartbeatRequest(clientIP+":"+clientPort,status,memoryAvailable);
        if (nameNodeChannel == null || !nameNodeChannel.isActive()){
            logger.error("Connection to NameNode lost, waiting......");
            connect();// 断线重连
        }else{
            nameNodeChannel.writeAndFlush(request);
            logger.debug("DataNode sent: " + request);
        }
    }

    public class ConnectionListener implements ChannelFutureListener {

        @Override
        public void operationComplete(ChannelFuture channelFuture) throws Exception {
            if (!channelFuture.isSuccess()) {
                logger.warn("connection to NameNode failed");
//                heartbeat.connect();// 3秒后重连
            }else {
                nameNodeChannel = channelFuture.channel();
                logger.info("Heartbeat connection established");
                run1(ONLINE);// 成功后就发起上线请求
            }
        }
    }
}
