package cn.mageek.namenode.handler;

import cn.mageek.common.command.Command;
import cn.mageek.common.model.RcvMsgObject;
import cn.mageek.common.model.WebMsgObject;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  处理server推送消息的handler，负责根据netMsg得到待发送消息并传递给下一个handler
 * @author Mageek Chiu
 * @date 2018/3/6 0006:19:59
 */
public class PushMsgHandler extends ChannelOutboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(PushMsgHandler.class);


    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        // 这种判断的方式比较浪费性能，是否有更优雅的解决方式？？？
        // 交换 PushMsgHandler 和 BusinessHandler 顺序即可，但是BusinessHandler必须处于最后？？使得回复的报文不需要经过PushMsgHandler
        // 可以再加一个InboundHandler
        if(msg instanceof WebMsgObject ){//是主动推送，需要编码
            WebMsgObject webMsgObject = (WebMsgObject)msg;;//根据消息字符串解析成消息对象
            RcvMsgObject rcvMsgObject =  ((Command)Class.forName("cn.mageek.common.command.Command"+webMsgObject.getCommand()).newInstance()).send(webMsgObject);
            logger.debug("pushMsg: {} to {}",rcvMsgObject,ctx.channel().remoteAddress());
//            super.write(ctx,rcvMsgObject,promise);
            ctx.writeAndFlush(rcvMsgObject);
        }else{
            logger.error("error pushMsg: {} to {}",msg,ctx.channel().remoteAddress());
        }
    }
}
