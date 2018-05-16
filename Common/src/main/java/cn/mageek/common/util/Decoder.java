package cn.mageek.common.util;

import cn.mageek.common.model.DataRequest;
import cn.mageek.common.model.DataResponse;
import cn.mageek.common.model.HeartbeatRequest;
import cn.mageek.common.model.HeartbeatResponse;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.EncoderException;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.LinkedList;
import java.util.List;
import static cn.mageek.common.model.LineType.*;
import static cn.mageek.common.res.Constants.*;

/**
 * 入站数据解码
 * @author Mageek Chiu
 * @date 2018/5/6 0007:16:20
 */
public class Decoder {

    private static final int MINIMAL_LENGTH = 6;
    private static final Logger logger = LoggerFactory.getLogger(Decoder.class);

    /**
     * 将接收到的bit数据解析为消息对象DataRequest的列表，兼容几个redis命令的协议
     * @param in 输入buffer
     * @return DataRequest
     */
    public static List<DataRequest> bytesToDataRequests(ByteBuf in) throws Exception{
        String msg = in.toString(CharsetUtil.UTF_8);
        if (in.readableBytes()<MINIMAL_LENGTH){
            throw new EncoderException("less than MINIMAL_LENGTH  "+msg);
        }
        return getDataRequests(msg);
    }

    // 简化的解码方式：要么7行(set)，要么5行(get、del、keys)，要么3行(COMMAND 也就是redis-cli 连接时发送的命令)
    private static List<DataRequest> getDataRequests(String s) throws Exception {

        List<DataRequest> dataRequestList = new LinkedList<>();

        String[] msgs = s.split(outerSplit);// 多个命令之间用 \t\n 分割  // 这个不属于redis，是我为了自己方便加的
        logger.debug("Decoder 获得 {} 条命令",msgs.length);
        for (String msg : msgs) {
            String[] strings = msg.split(innerSplit);// 单个命令内部用 \r\n 分割

            // 简单的校验
            int allLineNumber = strings.length;
            String ID = strings[allLineNumber-1];// Client发过来的才是ID,redis 协议没有这一段，所以可能是任何字段
            int ckvLineNumber = Integer.parseInt(strings[0].substring(1));
//            if (allLineNumber != 7 && allLineNumber != 5  && allLineNumber != 3) throw new Exception("all line number Exception");// 报文总行数
//            if (ckvLineNumber != 3 && ckvLineNumber != 2 && ckvLineNumber != 1) throw new Exception("command、key、value line number Exception");// command、key、value 的行数

            String command = strings[2].toUpperCase();// 命令全部转大写
            if (Integer.parseInt(strings[1].substring(1)) != command.length()) throw new Exception("command length Exception");
            if (command.equals("COMMAND")){// 没有 key
                dataRequestList.add(new DataRequest(command,"none","none",ID));
                continue;
            }

            // 有 key
            String key = strings[4];
            if (Integer.parseInt(strings[3].substring(1)) != key.length()) throw new Exception("key length Exception");

            String value = "none";// 没有 value
            if (allLineNumber >= 7){// 有 value
                value = strings[6];
                if (Integer.parseInt(strings[5].substring(1)) != value.length()) throw new Exception("value length Exception");
            }
            dataRequestList.add(new DataRequest(command,key,value,ID));
        }
        return dataRequestList;
    }

    /**
     * 将接收到的bit数据解析为消息对象DataResponse的列表，是redis协议的子集
     * @param in 输入buffer
     * @return DataRequest
     */
    public static List<DataResponse> bytesToDataResponse(ByteBuf in) throws Exception{

        List<DataResponse> responses = new LinkedList<>();

        String dataAll = in.toString(CharsetUtil.UTF_8);
        String[] datas = dataAll.split(outerSplit);
//        logger.debug("Decoder 获得 {} 条响应,{}",datas.length,dataAll);

        for (String data: datas){
            String[] lines = data.split(innerSplit);
            String ID = lines[lines.length-1];// Client请求的响应才是ID,redis 协议没有这一段，所以可能是任何字段
            String lineType = data.substring(0,1);
            logger.debug("Decoder 本条响应,lineType：{}",lineType);// keys 命令结果太长，导致DataHandler的channelRead触发了两次，这里自然解码出错,看长度正好在512 byte截断了，也就是分包了（与粘包相反），所以应该是可以配置的，结论是 RCVBUF_ALLOCATOR
            DataResponse response;
            String msg = "";
            List<String> msgList = new LinkedList<>();
            switch (lineType){
                case SINGLE_RIGHT:
                case SINGLE_ERROR:
                case INT_NUM:
                    msg = lines[0].substring(1);//+OK\r\n123123__@@__4r53243\r\n
                    break;
                case NEXT_LEN:
                    if( data.contains("-1")) msg="-1";// $-1\r\n123123__@@__4r53243\r\n
                    else msg = lines[1];// $3\r\nGGG\r\n123123__@@__4r53243\r\n
                    break;
                case LINE_NUM:
                    // *3\r\n$2\r\nff\r\n$3\r\nddd\r\n$4\r\ntttt\r\n123123__@@__4r53243\r\n
                    for (int i = 2;i<lines.length;i+=2) msgList.add(lines[i]);
                    break;
            }
            response = new DataResponse(lineType,msg,msgList,ID);
//            logger.debug("decoded response:{}",response);
            responses.add(response);
        }

        return responses;
    }

    /**
     * 将接收到的bit数据解析为消息对象HeartbeatRequest
     * @param in 输入buffer
     * @return DataRequest
     */
    public static HeartbeatRequest bytesToHeartbeatRequest(ByteBuf in) throws Exception{
        return new HeartbeatRequest("","", 10000000L);
    }
    /**
     * 将接收到的bit数据解析为消息对象HeartbeatResponse
     * @param in 输入buffer
     * @return DataRequest
     */
    public static HeartbeatResponse bytesToHeartbeatResponse(ByteBuf in) throws Exception{
        return new HeartbeatResponse(true,5,null);
    }

}
