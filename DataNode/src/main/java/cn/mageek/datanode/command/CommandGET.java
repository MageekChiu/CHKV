package cn.mageek.datanode.command;

import cn.mageek.common.command.Command;
import cn.mageek.common.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static cn.mageek.common.model.LineType.NEXT_LEN;


/**
 * @author Mageek Chiu
 * @date 2018/5/6 0007:13:49
 */
public class CommandGET extends Command {

    private static final Logger logger = LoggerFactory.getLogger(CommandGET.class);

    @Override
    public DataResponse receive(DataRequest dataRequest) {
        String answer = this.DATA_POOL.get(dataRequest.getKey());
        if(answer==null) return new DataResponse(NEXT_LEN,"-1");
        return new DataResponse(NEXT_LEN,answer);
    }

    @Override
    public DataResponse send(WebMsgObject webMsgObject) {
        return null;
    }



}
