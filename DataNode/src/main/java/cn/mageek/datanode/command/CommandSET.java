package cn.mageek.datanode.command;

import cn.mageek.common.command.AbstractDataNodeCommand;
import cn.mageek.common.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static cn.mageek.datanode.main.DataNode.DATA_POOL;

/**
 * 具体策略类
 * @author Mageek Chiu
 * @date 2018/5/6 0007:13:49
 */
public class CommandSET extends AbstractDataNodeCommand {

//    private static final Logger logger = LoggerFactory.getLogger(CommandSET.class);

    @Override
    public DataResponse receive(DataRequest dataRequest) {
        DATA_POOL.put(dataRequest.getKey(),dataRequest.getValue());
        return new DataResponse(LineType.SINGLE_RIGHT,"OK");
    }

    @Override
    public DataResponse send(WebMsgObject webMsgObject) {
        return null;
    }

}
