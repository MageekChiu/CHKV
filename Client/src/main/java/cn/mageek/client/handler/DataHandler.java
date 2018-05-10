package cn.mageek.client.handler;

import cn.mageek.client.job.Connection;
import cn.mageek.common.model.DataRequest;
import cn.mageek.common.model.DataResponse;
import cn.mageek.common.model.WatchResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 处理 来自NameNode 的 DataNode 监视事件
 * @author Mageek Chiu
 * @date 2018/3/10 0010:20:33
 */
public class DataHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(DataHandler.class);

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
        DataResponse response = (DataResponse) msg;//
        logger.debug("DataNode received: {}",response);
        ctx.close();// 收到响应就关闭连接
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("connection to: {}，error: ",ctx.channel().remoteAddress(),cause);
        ctx.close();
    }
}
