package cn.mageek.datanode.command;

import cn.mageek.common.command.AbstractDataNodeCommand;
import cn.mageek.common.model.DataRequest;
import cn.mageek.common.model.DataResponse;
import cn.mageek.common.model.WebMsgObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static cn.mageek.common.model.LineType.INT_NUM;
import static cn.mageek.common.model.LineType.LINE_NUM;
import static cn.mageek.common.res.Constants.innerSplit;
import static cn.mageek.datanode.main.DataNode.DATA_POOL;

/**

 * @author Mageek Chiu
 * @date 2018/5/6 0007:13:49
 */
public class CommandKEYS extends AbstractDataNodeCommand {

    private static final Logger logger = LoggerFactory.getLogger(CommandKEYS.class);

    @Override
    public DataResponse receive(DataRequest dataRequest) {

//        StringBuilder builder = new StringBuilder();
//        DATA_POOL.forEach((k,v)->{
//            builder.append("$").append(k.length()).append(innerSplit).append(k).append(innerSplit);// 这样就和协议耦合了，应该定义一个list的结果，让eocoder自己去编码，才能和协议解耦
//        });
//        return new DataResponse(LINE_NUM,builder.toString());//

        List<String> msgList;

        String  key = dataRequest.getKey();
        if (key.equals("*")){
            msgList = new ArrayList<>(DATA_POOL.keySet());
        }else {
//        DATA_POOL.forEach((k,v)->{
//            if (k.startsWith(dataRequest.getKey()))
//                msgList.add(k);
//        });
            msgList = DATA_POOL.keySet().stream().filter((k)->k.startsWith(dataRequest.getKey())).collect(Collectors.toList());
        }

        return new DataResponse(LINE_NUM,msgList);//
    }

    @Override
    public DataResponse send(WebMsgObject webMsgObject) {
        return null;
    }

}
