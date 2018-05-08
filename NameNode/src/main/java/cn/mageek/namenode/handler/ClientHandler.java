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
public class ClientHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);
    private static final AtomicInteger dataNodeNumber = new AtomicInteger(0);

    private Map<String,Channel> dataNodeMap;
    private Map<String,Channel> clientMap;

    public ClientHandler(Map<String,Channel> dataNodeMap, Map<String,Channel> clientMap){
        this.dataNodeMap = dataNodeMap;
        this.clientMap = clientMap;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        String uuid = ctx.channel().id().asLongText();
        dataNodeMap.put(uuid,ctx.channel());
        logger.info("new connection arrived: {},uuid:{}, clients living {}",ctx.channel().remoteAddress(),uuid, dataNodeNumber.incrementAndGet());//包含ip:port
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String uuid = ctx.channel().id().asLongText();
        dataNodeMap.remove(uuid);
        logger.info("connection closed: {},uuid:{}, clients living {}",ctx.channel().remoteAddress(),uuid, dataNodeNumber.decrementAndGet());//包含ip:port
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("receiveMsg from: {}，error: ",ctx.channel().remoteAddress(),cause);
        ctx.close();
    }
}
