package cn.mageek.namenode.handler;

import cn.mageek.common.model.WatchRequest;
import cn.mageek.common.model.WatchResponse;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

import static cn.mageek.namenode.main.NameNode.clientMap;
import static cn.mageek.namenode.main.NameNode.sortedServerMap;

/**
 * @author Mageek Chiu
 * @date 2018/5/7 0007:13:52
 */
public class ClientWatcherHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(ClientWatcherHandler.class);
//    private ConcurrentSkipListMap<Integer, String> sortedServerMap;//管理所有datanode key:DataNode hash ,value: IP:port
//    private Map<String,Channel> clientMap;// 管理所有client，key ip:port,value chanel

//    public ClientWatcherHandler(ConcurrentSkipListMap<Integer, String> sortedServerMap, Map<String,Channel> clientMap){
//        this.sortedServerMap = sortedServerMap;
//        this.clientMap = clientMap;
//    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        String connection = ctx.channel().remoteAddress().toString();
        clientMap.put(connection,ctx.channel());
        logger.info("new connection arrived: {} clients living {}",connection, clientMap.size());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String connection = ctx.channel().remoteAddress().toString();
        clientMap.remove(connection);
        logger.info("connection closed: {},uuid:{}, clients living {}",connection, clientMap.size());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        WatchRequest request = (WatchRequest)msg ;
        logger.debug("NameNode received: {}" , request);
        if (request.isImmediately()){// 需要立即回复，一般是刚上线的时候；否则就等节点变化时NameNode主动通知就行了
            WatchResponse response = new WatchResponse(sortedServerMap);
            logger.debug("NameNode answered: {}" , response);
            ctx.writeAndFlush(response);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("receiveMsg from: {}，error: ",ctx.channel().remoteAddress(),cause);
        ctx.close();
    }
}
