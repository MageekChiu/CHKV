package cn.mageek.datanode.jobs;

import cn.mageek.common.model.DataRequest;
import cn.mageek.datanode.handler.DataTransferHandler;
import cn.mageek.datanode.handler.HeartBeatHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 向dataNode转移数据
 * @author Mageek Chiu
 * @date 2018/5/7 0007:10:44
 */
public class DataTransfer extends DataRunnable{
    private static final Logger logger = LoggerFactory.getLogger(DataTransfer.class);

    private boolean isAll;
    private String dataNodeIP;
    private String dataNodePort;
    private String dataNodeIPPort;

    @Override
    public void run(){
//        logger.debug("DataTransfer instance {},dataPool {}",this,DATA_POOL);
        // 其他节点上线能正确传输，然后其他节点再上线也能正确传输
        // 其他节点上线能正确传输，然后自己下线就不能正确传输了,DataTransferHandler 里面 DATA_POOL为 null，但是这里每次都不是null,和handler周期有关？

        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap b = new Bootstrap();
        EventExecutorGroup businessGroup = new DefaultEventExecutorGroup(1);//处理耗时业务逻辑，不占用IO线程
        b.group(group)
                .channel(NioSocketChannel.class)
                .remoteAddress(new InetSocketAddress(dataNodeIP, Integer.parseInt(dataNodePort)))
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch)  throws Exception {
                        ChannelPipeline p = ch.pipeline();
//                        p.addLast(new ObjectDecoder(2048, ClassResolvers.cacheDisabled(this.getClass().getClassLoader())));// in 进制缓存类加载器
//                        p.addLast(new ObjectEncoder());// out
                        p.addLast(businessGroup,"DataTransferHandler",new DataTransferHandler(dataNodeIPPort,DATA_POOL,isAll));// in
                    }
                });
        try {
            ChannelFuture f = b.connect().sync();// 发起连接,阻塞等待
            logger.debug("DataTransfer connection established");
            f.channel().closeFuture().sync();// 这是一段阻塞的代码，除非链路断了，否则是不会停止阻塞的，我们可以在handler中手动关闭，达到关闭客户端的效果
            group.shutdownGracefully().sync();
            logger.debug("DataTransfer connection closed");
        } catch (InterruptedException e) {
            logger.error("DataTransfer connection InterruptedException",e);
        }
    }

//    @Override
//    public void connect(Map<String,String> dataPool){
//        this.DATA_POOL = dataPool;
//    }

    /**
     * 转移数据
     * @param nextIPPort 转移到目标节点
     * @param isAll 是否全部转移
     */
    public void connect(String nextIPPort,boolean isAll){
        dataNodeIPPort = nextIPPort;
        String[] strings = dataNodeIPPort.split(":");
        dataNodeIP = strings[0];
        dataNodePort = strings[1];
        this.isAll = isAll;
    }

}
