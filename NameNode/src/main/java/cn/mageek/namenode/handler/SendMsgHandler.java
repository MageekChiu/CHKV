package cn.mageek.namenode.handler;

import cn.mageek.CHKV.util.Encoder;
import cn.mageek.common.model.RcvMsgObject;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  处理server发出给client的消息的handler，负责把消息对象转换为buffer并发送给客户端
 * @author Mageek Chiu
 * @date 2018/3/6 0006:19:59
 */
public class SendMsgHandler extends ChannelOutboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(SendMsgHandler.class);


    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        RcvMsgObject rcvMsgObject = (RcvMsgObject)msg;
        ByteBuf buf = Encoder.objectToBytes(rcvMsgObject);//把消息对象RcvMsgObject转换为buffer
        logger.debug("sendMsg: {} to {}",rcvMsgObject,ctx.channel().remoteAddress());
        ctx.writeAndFlush(buf);
        promise.setSuccess();

    }

}
