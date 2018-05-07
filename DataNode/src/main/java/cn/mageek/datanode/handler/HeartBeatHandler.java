package cn.mageek.datanode.handler;

import cn.mageek.common.model.HeartbeatRequest;
import cn.mageek.common.model.HeartbeatResponse;
import cn.mageek.common.util.Encoder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Mageek Chiu
 * @date 2018/5/7 0007:13:52
 */
public class HeartBeatHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(HeartBeatHandler.class);

    private String clientIP;
    private String clientPort;

    public HeartBeatHandler(String clientIP, String clientPort) {
        this.clientIP = clientIP;
        this.clientPort = clientPort;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("opened connection to: {}",ctx.channel().remoteAddress());
        long memoryAvailable = Runtime.getRuntime().freeMemory();
//        long cpuAvailable = Runtime.getRuntime().availableProcessors();
        HeartbeatRequest request = new HeartbeatRequest(clientIP+":"+clientPort,memoryAvailable);
//        ByteBuf buf = Encoder.heartbeatRequestToBytes(request);
//        ctx.writeAndFlush(Unpooled.copiedBuffer(buf));
        logger.debug("DataNode sent: " + request);
        ctx.writeAndFlush(request);// 因为这个in 上面的 out有decoder，所以可以直接发送对象，而不需要自己再写一遍转为bytebuf的encoder
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info("closed connection: {}",ctx.channel().remoteAddress());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        HeartbeatResponse response = (HeartbeatResponse) msg;// 因为这个in 上面的 in 是decoder，所以直接可以获得对象
        logger.debug("DataNode received: " + response);
        ctx.close();// 收到相应关闭连接
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("connection to: {}，error: ",ctx.channel().remoteAddress(),cause);
        ctx.close();
    }
}
