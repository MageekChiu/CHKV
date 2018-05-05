package cn.mageek.CHKV.util;

import cn.mageek.common.model.RcvMsgObject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * 出站数据编码
 * @author Mageek Chiu
 * @date 2018/3/7 0007:18:53
 */
public class Encoder {

    private static final int MINIMAL_LENGTH = 24;
    private static final Logger logger = LoggerFactory.getLogger(Encoder.class);
    /**
     * 将消息对象解析为bit数据
     * @param  msgObject
     * @return
     */
    public static ByteBuf objectToBytes(RcvMsgObject msgObject) throws IOException {
        ByteBuf byteBuf = Unpooled.buffer();
        return byteBuf;
    }
}
