package cn.mageek.common.command;

import cn.mageek.common.model.RcvMsgObject;
import cn.mageek.common.model.WebMsgObject;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Mageek Chiu
 * @date 2018/3/7 0007:19:29
 */
public class Command81 implements Command {

    private static final Logger logger = LoggerFactory.getLogger(Command81.class);

    @Override
    public RcvMsgObject receive(RcvMsgObject msgObject) {
            return new RcvMsgObject("M:","1234567899","1","81","1",(short)2,1234567890, Unpooled.copiedBuffer("4234234", CharsetUtil.UTF_8),"2");
    }

    @Override
    public RcvMsgObject send(WebMsgObject webMsgObject) {
        logger.debug("合成消息，{}",webMsgObject);
        return new RcvMsgObject("M:","1234567899","1","81","1",(short)2,1234567890, Unpooled.copiedBuffer("4234234", CharsetUtil.UTF_8),"2");
    }


}
