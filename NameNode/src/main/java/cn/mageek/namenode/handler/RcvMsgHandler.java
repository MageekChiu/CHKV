package cn.mageek.namenode.handler;

import cn.mageek.CHKV.util.Decoder;
import cn.mageek.common.model.RcvMsgObject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * server收到的buffer转换为消息对象
 * @author Mageek Chiu
 * @date 2018/3/10 0005:14:32
 */
public class RcvMsgHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(RcvMsgHandler.class);


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        ByteBuf buf  = (ByteBuf) msg;
        try {
            logger.info("receiveMsg: {} ,from: {}",ByteBufUtil.hexDump(buf),ctx.channel().remoteAddress());
            // 下面都属于自己的业务逻辑 如果有耗时操作是不能直接放在这里的，否则阻塞了IO线程可能会影响其它channel从而影响整体吞吐量，所以需要分离出去
            // 将 buffer 解析成 对象 并转发
            RcvMsgObject msgObject = Decoder.bytesToObject(buf);
            logger.debug("parsed data:\n{}",msgObject);
            ctx.fireChannelRead(msgObject);//传输到下一个inBound

        }catch (Exception e){
            logger.error("parse data :{} , from: {} , error: ", ByteBufUtil.hexDump(buf),ctx.channel().remoteAddress(),e);
        }finally {
//            buf 如果在上面被发送到另一个Handler了（用了write），这里就不能释放了，因为释放buf已经变成了另一个Handler或者自定义对象（如上面的Decoder）的责任了，这里再释放就会报错
//            ReferenceCountUtil.release(buf); // 引用计数，清除引用，便于释放内存
//            buf.release();// 和上面作用一样
        }
    }

}
