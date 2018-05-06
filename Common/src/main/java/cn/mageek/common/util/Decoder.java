package cn.mageek.common.util;

import cn.mageek.common.model.DataRequest;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.EncoderException;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 入站数据解码
 * @author Mageek Chiu
 * @date 2018/5/6 0007:16:20
 */
public class Decoder {

    private static final int MINIMAL_LENGTH = 6;
    private static final Logger logger = LoggerFactory.getLogger(Decoder.class);

    /**
     * 将接收到的bit数据解析为消息对象，是redis协议的子集
     * @param in 输入buffer
     * @return DataRequest
     */
    public static DataRequest bytesToObject(ByteBuf in) throws Exception{

        if (in.readableBytes()<MINIMAL_LENGTH){
            throw new EncoderException("less than MINIMAL_LENGTH");
        }
        String msg = in.toString(CharsetUtil.UTF_8);
        return getDataRequest(msg);
    }

    // 简化的解码方式：要么7行(set)，要么5行(get、del)，要么3行(COMMAND 也就是redis-cli 连接时发送的命令)
    private static DataRequest getDataRequest(String msg) throws Exception {

        String[] strings = msg.split("\r\n");
//        for (String string : strings) { logger.debug(string); }
        int allLineNumber = strings.length;
        int ckvLineNumber = Integer.parseInt(strings[0].substring(1));
        String value = "none";
        if (allLineNumber != 7 && allLineNumber != 5  && allLineNumber != 3) throw new Exception("all line number Exception");// 报文总行数
        if (ckvLineNumber != 3 && ckvLineNumber != 2 && ckvLineNumber != 1) throw new Exception("command、key、value line number Exception");// command、key、value 的行数

        String command = strings[2].toUpperCase();// 命令全部转大写
        if (Integer.parseInt(strings[1].substring(1)) != command.length()) throw new Exception("command length Exception");
        if (command.equals("COMMAND")) return new DataRequest(command,"","");

        String key = strings[4];
        if (Integer.parseInt(strings[3].substring(1)) != key.length()) throw new Exception("key length Exception");

        if (allLineNumber == 7){
            value = strings[6];
            if (Integer.parseInt(strings[5].substring(1)) != value.length()) throw new Exception("value length Exception");
        }

        logger.debug(new DataRequest(command,key,value).toString());
        return new DataRequest(command,key,value);
    }

}
