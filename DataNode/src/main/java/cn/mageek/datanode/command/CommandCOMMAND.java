package cn.mageek.datanode.command;

import cn.mageek.common.command.AbstractDataNodeCommand;
import cn.mageek.common.model.DataRequest;
import cn.mageek.common.model.DataResponse;
import cn.mageek.common.model.WebMsgObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static cn.mageek.common.model.LineType.INT_NUM;

/**
 * @author Mageek Chiu
 * @date 2018/5/6 0007:13:49
 */
public class CommandCOMMAND extends AbstractDataNodeCommand {

    private static final Logger logger = LoggerFactory.getLogger(CommandCOMMAND.class);

    @Override
    public DataResponse receive(DataRequest dataRequest) {
        return new DataResponse(INT_NUM,"1");//
    }

    @Override
    public DataResponse send(WebMsgObject webMsgObject) {
        return null;
    }

}
