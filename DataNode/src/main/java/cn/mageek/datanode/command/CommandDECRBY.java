package cn.mageek.datanode.command;

import cn.mageek.common.command.AbstractDataNodeCommand;
import cn.mageek.common.model.DataRequest;
import cn.mageek.common.model.DataResponse;
import cn.mageek.common.model.LineType;
import cn.mageek.common.model.WebMsgObject;

import static cn.mageek.datanode.main.DataNode.DATA_POOL;

/**
 * 单增 用CAS保证原子性
 * 这种当前操作依赖于之前值的命令都要这样保证
 * @author Mageek Chiu
 * @date 2018/5/6 0007:13:49
 */
public class CommandDECRBY extends AbstractDataNodeCommand {

//    private static final Logger logger = LoggerFactory.getLogger(CommandSET.class);

    @Override
    public DataResponse receive(DataRequest dataRequest) {
        String key = dataRequest.getKey();
        String val = "-"+dataRequest.getValue();
        return CommandINCRBY.incrBy(key,val);
    }

    @Override
    public DataResponse send(WebMsgObject webMsgObject) {
        return null;
    }


}
