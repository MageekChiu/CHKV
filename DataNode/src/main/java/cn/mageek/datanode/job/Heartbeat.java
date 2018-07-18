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
import java.net.InetSocketAddress;

import static cn.mageek.common.model.HeartbeatType.*;
import static cn.mageek.datanode.main.DataNode.*;
import static cn.mageek.datanode.service.CronJobManager.isMaster;
import static cn.mageek.datanode.service.CronJobManager.useHA;


/**
 * 向namenode发送心跳
 * @author Mageek Chiu
 * @date 2018/5/7 0007:10:44
 */
public class Heartbeat extends DataRunnable{

    private static final Logger logger = LoggerFactory.getLogger(Heartbeat.class);

    private static EventLoopGroup group ;
    private static Channel nameNodeChannel = null;

//    static { // 初始化
////        JVM自定义参数通过java命令的可选项:
////        -D<name>=<value>     如 java -Ddatanode.client.ip=192.168.0.136 -Ddatanode.client.port=10099 DataNode
////                来传入JVM，传入的参数作为system的property。因此在程序中可以通过下面的语句获取参数值：
////        System.getProperty(<name>)
//    }

    // 实际连接
    @Override
    public void connect(){
        if (nameNodeChannel != null && nameNodeChannel.isActive()) return;// 防止多次重连
        // 这里必须new 否则可能就会失败多次被回收了
        group = new NioEventLoopGroup();
        Bootstrap b = new Bootstrap();

        b.group(group)
                .channel(NioSocketChannel.class)
//                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,5000)// 设置超时
                .remoteAddress(new InetSocketAddress(nameNodeIP, Integer.parseInt(nameNodePort)))// 配置namenode ip port
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) {
                        ChannelPipeline p = ch.pipeline();
                        p.addLast(new ObjectDecoder(2048, ClassResolvers.cacheDisabled(this.getClass().getClassLoader())));// in 进制缓存类加载器
                        p.addLast(new ObjectEncoder());// out
                        p.addLast("HeartBeatHandler",new HeartBeatHandler());// in
                    }
                });

        ChannelFuture f= b.connect();
        f.addListener((ChannelFutureListener) channelFuture -> {
            if (!channelFuture.isSuccess()) {
                logger.warn("connection to NameNode failed");
                group.shutdownGracefully();
            }else {
                nameNodeChannel = channelFuture.channel();
                logger.info("Heartbeat connection established");
                run1(ONLINE);// 成功后就发起上线请求
                dataNodeStatus = ONLINE;
            }
        });// 连接监听器
        ChannelFuture future = f.channel().closeFuture();// 采用异步加回调函数的做法，防止阻塞
        future.addListener((ChannelFutureListener) channelFuture -> {// 关闭成功，主动或者被动
            group.shutdownGracefully().sync();
            nameNodeChannel = null;
            logger.debug("Heartbeat connection closed");
        });
    }

    public void disconnect(){
        nameNodeChannel.close();
    }

    public boolean isConnected(){
        return nameNodeChannel!=null&&nameNodeChannel.isActive();
    }

    @Override
    public void run(){
        run1(RUNNING);// 运行中发送心跳
        dataNodeStatus = RUNNING;
    }

    /**
     * 发送不同状态的心跳
     * @param status 状态
     */
    public void run1(String status){
        if (!useHA || isMaster) {// 不使用HA 或者使用HA但是是master才发送心跳
            long memoryAvailable = Runtime.getRuntime().freeMemory();
            HeartbeatRequest request = new HeartbeatRequest(clientIP+":"+clientPort,status,memoryAvailable);
            if (nameNodeChannel == null || !nameNodeChannel.isActive()){
                logger.error("Connection to NameNode lost, retrying......");
                connect();// 断线重连
            }else{
                nameNodeChannel.writeAndFlush(request);
                logger.debug("DataNode sent: " + request);
            }
        }
    }

}
