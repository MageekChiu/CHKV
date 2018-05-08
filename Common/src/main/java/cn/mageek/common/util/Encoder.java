package cn.mageek.common.util;

import cn.mageek.common.model.DataRequest;
import cn.mageek.common.model.DataResponse;
import cn.mageek.common.model.HeartbeatRequest;
import cn.mageek.common.model.HeartbeatResponse;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static cn.mageek.common.model.LineType.*;

/**
 * 出站数据编码
 * @author Mageek Chiu
 * @date 2018/5/6 0007:16:21
 */
public class Encoder {

    private static final Logger logger = LoggerFactory.getLogger(Encoder.class);
    private static final String spliter = "\r\n";
    /**
     * 将消息对象DataResponse编码bit数据
     * @param  dataResponse 待编码对象
     * @return 返回buffer
     */
    public static ByteBuf dataResponseToBytes(DataResponse dataResponse){
        String response = "+OK"+spliter;
        String lineType = dataResponse.getLineType(),msg = dataResponse.getMsg();
        switch (dataResponse.getLineType()){
            case SINGLE_RIGHT:
            case SINGLE_ERROR:
            case INT_NUM:
                response = lineType+msg+spliter;// 以上三种格式一致
                break;
            case NEXT_LEN:
                if( msg.equals("-1")) response = lineType+"-1"+spliter;// 未找到就直接-1，每一行结束都要有\r\n
                else response = lineType+msg.length()+spliter+msg+spliter;
                break;
            case LINE_NUM:
                break;
        }
        logger.debug("response:{}",response);
        return Unpooled.copiedBuffer(response,CharsetUtil.UTF_8);
    }

    /**
     * 将消息对象dataRequest编码bit数据
     * @param  dataRequest 待编码对象
     * @return 返回buffer
     */
    public static ByteBuf dataRequestToBytes(DataRequest dataRequest){
        String request = "";
        String key = dataRequest.getKey();
        String value = dataRequest.getValue();
        switch (dataRequest.getCommand()){
            case "SET":
                request = "*3"+spliter+"$3"+spliter+"SET"+spliter+"$"+key.length()+spliter+key+spliter+"$"+value.length()+spliter+value+spliter;
                break;
            case "GET":
                request = "*2"+spliter+"$3"+spliter+"GET"+spliter+"$"+key.length()+spliter+key+spliter;
                break;
            case "DEL":
                request = "*2"+spliter+"$3"+spliter+"DEL"+spliter+"$"+key.length()+spliter+key+spliter;
                break;
            case "COMMAND":
                break;
        }
        return Unpooled.copiedBuffer(request,CharsetUtil.UTF_8);
    }

    /**
     * 将消息对象HeartbeatResponse编码为bit数据
     * @param  heartbeatResponse 待编码对象
     * @return 返回buffer
     */
    public static ByteBuf heartbeatResponseToBytes(HeartbeatResponse heartbeatResponse){
        String response = "";
        return Unpooled.copiedBuffer(response,CharsetUtil.UTF_8);
    }

    /**
     * 将消息对象HeartbeatRequest编码为bit数据
     * @param  heartbeatRequest 待编码对象
     * @return 返回buffer
     */
    public static ByteBuf heartbeatRequestToBytes(HeartbeatRequest heartbeatRequest){
        String response = "";
        return Unpooled.copiedBuffer(response,CharsetUtil.UTF_8);
    }
}
