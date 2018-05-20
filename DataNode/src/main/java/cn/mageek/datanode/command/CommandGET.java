package cn.mageek.datanode.command;

import cn.mageek.common.command.AbstractDataNodeCommand;
import cn.mageek.common.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static cn.mageek.common.model.LineType.NEXT_LEN;
import static cn.mageek.datanode.main.DataNode.DATA_EXPIRE;
import static cn.mageek.datanode.main.DataNode.DATA_POOL;


/**
 * @author Mageek Chiu
 * @date 2018/5/6 0007:13:49
 */
public class CommandGET extends AbstractDataNodeCommand {

//    private static final Logger logger = LoggerFactory.getLogger(CommandGET.class);

    @Override
    public DataResponse receive(DataRequest dataRequest) {
        String key = dataRequest.getKey();

        Long expireTime = DATA_EXPIRE.get(key);
        if (expireTime != null && ( expireTime< System.currentTimeMillis() )){// 已经过期
            DATA_POOL.remove(key);
            DATA_EXPIRE.remove(key);
            return new DataResponse(NEXT_LEN,"-1");
        }

        String answer = DATA_POOL.get(key);
        if(answer==null) return new DataResponse(NEXT_LEN,"-1");// 键不存在
        return new DataResponse(NEXT_LEN,answer);
    }

    @Override
    public DataResponse send(WebMsgObject webMsgObject) {
        return null;
    }



}
