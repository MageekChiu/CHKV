package cn.mageek.common.command;

import cn.mageek.common.model.*;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 具体策略类
 * @author Mageek Chiu
 * @date 2018/5/6 0007:13:49
 */
public class CommandSET extends Command {

    private static final Logger logger = LoggerFactory.getLogger(CommandSET.class);

    @Override
    public DataResponse receive(DataRequest dataRequest) {
        try {
//            logger.debug("DATA_POOL.size:{}",this.DATA_POOL.size());logger.debug("dataRequest:{}",dataRequest);
            this.DATA_POOL.put(dataRequest.getKey(),dataRequest.getValue());
            return new DataResponse(LineType.SINGLE_RIGHT,"OK");
        }catch (Exception e){
            logger.error("set error:",e);
            return new DataResponse(LineType.SINGLE_ERROR,e.getMessage());
        }
    }

    @Override
    public DataResponse send(WebMsgObject webMsgObject) {
        return null;
    }

}
