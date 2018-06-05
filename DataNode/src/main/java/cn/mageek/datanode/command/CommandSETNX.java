package cn.mageek.datanode.command;

import cn.mageek.common.command.AbstractDataNodeCommand;
import cn.mageek.common.model.DataRequest;
import cn.mageek.common.model.DataResponse;
import cn.mageek.common.model.LineType;
import cn.mageek.common.model.WebMsgObject;

import static cn.mageek.datanode.main.DataNode.DATA_POOL;

/**
 * 具体策略类
 * @author Mageek Chiu
 * @date 2018/5/6 0007:13:49
 */
public class CommandSETNX extends AbstractDataNodeCommand {

//    private static final Logger logger = LoggerFactory.getLogger(CommandSET.class);

    @Override
    public DataResponse receive(DataRequest dataRequest) {

        String key = dataRequest.getKey();
        String value = dataRequest.getValue();

        String res = DATA_POOL.putIfAbsent(key, value);// 不存在则存储并返回null，否则返回已存在的value
        return new DataResponse(LineType.INT_NUM,res==null?"1":"0");
    }

    @Override
    public DataResponse send(WebMsgObject webMsgObject) {
        return null;
    }

}
