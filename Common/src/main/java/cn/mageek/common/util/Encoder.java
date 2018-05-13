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

import java.util.List;

import static cn.mageek.common.model.LineType.*;
import static cn.mageek.common.res.Constants.IDSplitter;
import static cn.mageek.common.res.Constants.innerSplit;
import static cn.mageek.common.res.Constants.outerSplit;

/**
 * 出站数据编码
 * @author Mageek Chiu
 * @date 2018/5/6 0007:16:21
 */
public class Encoder {

    private static final Logger logger = LoggerFactory.getLogger(Encoder.class);

    /**
     * 将消息对象DataResponse编码bit数据，这里不需要List<DataResponse> 是因为解码 的时候把多个命令拆分了，分成多次回复，这样每次就只需要回复一个DataResponse就行，redis里面是有Bulk Reply的，也就是LINE_NUM
     * @param  dataResponse 待编码对象
     * @return 返回buffer
     */
    public static ByteBuf dataResponseToBytes(DataResponse dataResponse){
        String response = "+OK"+ innerSplit;
        String lineType = dataResponse.getLineType(),msg = dataResponse.getMsg(),ID = dataResponse.getID();
        // 兼容redis协议
        switch (dataResponse.getLineType()){
            case SINGLE_RIGHT://+
            case SINGLE_ERROR://-
            case INT_NUM:// :
                response = lineType+msg+ innerSplit;// 以上三种格式一致
                break;
            case NEXT_LEN:// $
                if( msg.equals("-1")) response = lineType+"-1"+ innerSplit;// 未找到就直接-1，每一行结束都要有\r\n
                else response = lineType+msg.length()+ innerSplit +msg+ innerSplit;
                break;
            case LINE_NUM:// *
                break;
        }
        if (ID.contains(IDSplitter)){// Client 发来的，需要补上ID
            response += (outerSplit +ID);
        }
//        logger.debug("response:{}",response);
        return Unpooled.copiedBuffer(response,CharsetUtil.UTF_8);
    }

    /**
     * 将消息对象dataRequest编码bit数据
     * @param  dataRequests 待编码对象列表
     * @return 返回buffer
     */
    public static ByteBuf dataRequestToBytes(List<DataRequest> dataRequests){
        StringBuilder requests = new StringBuilder();// 不保证线程安全，但是这里不需要
        for (DataRequest dataRequest : dataRequests) {
            String request = "";
            String key = dataRequest.getKey();
            String value = dataRequest.getValue();
            switch (dataRequest.getCommand()){
                case "SET":
                    request = "*3"+ innerSplit +"$3"+ innerSplit +"SET"+ innerSplit +"$"+key.length()+ innerSplit +key+ innerSplit +"$"+value.length()+ innerSplit +value+ innerSplit ;
                    break;
                case "GET":
                    request = "*2"+ innerSplit +"$3"+ innerSplit +"GET"+ innerSplit +"$"+key.length()+ innerSplit +key+ innerSplit ;
                    break;
                case "DEL":
                    request = "*2"+ innerSplit +"$3"+ innerSplit +"DEL"+ innerSplit +"$"+key.length()+ innerSplit +key+ innerSplit ;
                    break;
                case "COMMAND":
                    request = "*1"+ innerSplit +"$7"+ innerSplit +"COMMAND"+ innerSplit ;
                    break;
            }
            request += (dataRequest.getID()+innerSplit);// 补上ID，但是就不加字符长度了，不属于redis
            request += outerSplit;// 补上多条命令之间的分隔符
            requests.append(request);
        }
        String finalString = requests.toString();
//        finalString = finalString.substring(0,finalString.length()-outerSplit.length());// 去掉最后一个 outerSplit
//        logger.debug("final dataRequests string {},length:{},request num:{}",finalString,finalString.length(),dataRequests.size());
        return Unpooled.copiedBuffer(finalString,CharsetUtil.UTF_8);
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
