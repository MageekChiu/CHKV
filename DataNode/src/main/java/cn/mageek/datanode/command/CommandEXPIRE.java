package cn.mageek.datanode.command;

import cn.mageek.common.command.AbstractDataNodeCommand;
import cn.mageek.common.model.DataRequest;
import cn.mageek.common.model.DataResponse;
import cn.mageek.common.model.LineType;
import cn.mageek.common.model.WebMsgObject;

import static cn.mageek.datanode.main.DataNode.DATA_EXPIRE;
import static cn.mageek.datanode.main.DataNode.DATA_POOL;

/**
 * 具体策略类
 * @author Mageek Chiu
 * @date 2018/5/6 0007:13:49
 */
public class CommandEXPIRE extends AbstractDataNodeCommand {

//    private static final Logger logger = LoggerFactory.getLogger(CommandSET.class);

    @Override
    public DataResponse receive(DataRequest dataRequest) {

        if (!DATA_POOL.containsKey(dataRequest.getKey())) return new DataResponse(LineType.INT_NUM,"0");//不存在返回0

        long seconds = Long.parseLong((dataRequest.getValue()))*1000;// 待存活秒数*1000 亦即 存活毫秒数
        seconds += (System.currentTimeMillis()); // 存活截止时间戳，单位毫秒
        DATA_EXPIRE.put(dataRequest.getKey(),seconds);//
        return new DataResponse(LineType.INT_NUM,"1");// 存在返回设置成功的数量
    }

    @Override
    public DataResponse send(WebMsgObject webMsgObject) {
        return null;
    }

}
