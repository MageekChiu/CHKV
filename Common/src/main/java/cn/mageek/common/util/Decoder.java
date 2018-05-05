package cn.mageek.CHKV.util;

import cn.mageek.common.model.RcvMsgObject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.EncoderException;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * 入站数据解码
 * @author Mageek Chiu
 * @date 2018/3/7 0007:18:52
 */
public class Decoder {

    private static final int MINIMAL_LENGTH = 24;
    private static final Logger logger = LoggerFactory.getLogger(Decoder.class);

    /**
     * 将接收到的bit数据解析为消息对象
     * @param in
     * @return
     */
    public static RcvMsgObject bytesToObject(ByteBuf in) throws IOException{

        if (in.readableBytes()<MINIMAL_LENGTH){
            throw new EncoderException("less than MINIMAL_LENGTH");
        }
        String header = (in.readBytes(2)).toString(CharsetUtil.UTF_8);
//        logger.debug("header "+header);
        String mac = (in.readBytes(12)).toString(CharsetUtil.UTF_8);
//        logger.debug("mac "+mac);
        String command = ByteBufUtil.hexDump(in.readBytes(1));
//        logger.debug("command "+command);
        String para = ByteBufUtil.hexDump(in.readBytes(1));
//        logger.debug("para "+para);
        int timestamp = in.readIntLE();
//        logger.debug("timestamp "+timestamp);
        short dataLength = in.readShortLE();
//        logger.debug("dataLength "+dataLength);
        ByteBuf data = Unpooled.copiedBuffer(in.readBytes(dataLength));
//        logger.debug("data "+ data.toString(CharsetUtil.US_ASCII));
        String footer = ByteBufUtil.hexDump(in.readBytes(1));
        in.release();
        return new RcvMsgObject(header, mac, "1", command, para, dataLength, timestamp, data, footer);
    }

}
