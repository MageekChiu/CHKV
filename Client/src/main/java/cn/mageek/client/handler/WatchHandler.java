package cn.mageek.client.handler;

import cn.mageek.client.Connection;
import cn.mageek.common.model.WatchResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 处理 来自NameNode 的 DataNode 监视事件
 * @author Mageek Chiu
 * @date 2018/3/10 0010:16:22
 */
public class WatchHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(WatchHandler.class);

    private Connection connection;

    public WatchHandler(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("opened connection to: {}",ctx.channel().remoteAddress());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info("closed connection: {}",ctx.channel().remoteAddress());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        WatchResponse response = (WatchResponse) msg;//
        logger.debug("received DataNode list: {}",response);
        // 修改本地的hash环
        connection.sortedServerMap = response.getHashCircle();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("connection to: {}，error: ",ctx.channel().remoteAddress(),cause);
        ctx.close();
    }
}
