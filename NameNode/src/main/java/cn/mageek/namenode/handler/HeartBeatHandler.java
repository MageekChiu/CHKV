package cn.mageek.namenode.handler;

import cn.mageek.common.model.HeartbeatRequest;
import cn.mageek.common.model.HeartbeatResponse;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Mageek Chiu
 * @date 2018/5/7 0007:13:52
 */
public class HeartBeatHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(HeartBeatHandler.class);
    private static final AtomicInteger clientNumber = new AtomicInteger(0);
    // 下面的 field 是线程安全的,由manager传入的concurrentHashMap，所以多个线程修改是没有问题的。
    private Map<String,Channel> channelMap;

    public HeartBeatHandler(final Map<String,Channel> channelMap){
        this.channelMap = channelMap;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        String uuid = ctx.channel().id().asLongText();
        channelMap.put(uuid,ctx.channel());
        logger.info("new connection arrived: {},uuid:{}, clients living {}",ctx.channel().remoteAddress(),uuid,clientNumber.incrementAndGet());//包含ip:port
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String uuid = ctx.channel().id().asLongText();
        channelMap.remove(uuid);
        logger.info("connection closed: {},uuid:{}, clients living {}",ctx.channel().remoteAddress(),uuid,clientNumber.decrementAndGet());//包含ip:port
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        HeartbeatRequest request = (HeartbeatRequest)msg ;
        logger.debug("NameNode received: " + request);
        HeartbeatResponse response = new HeartbeatResponse(true,clientNumber.get(),null);// 正常，不需要数据迁移
        logger.debug("NameNode answered: " + response);
        ctx.writeAndFlush(response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("receiveMsg from: {}，error: ",ctx.channel().remoteAddress(),cause);
        ctx.close();
    }
}
