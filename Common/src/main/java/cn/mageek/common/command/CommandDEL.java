package cn.mageek.common.command;

import cn.mageek.common.model.DataRequest;
import cn.mageek.common.model.DataResponse;
import cn.mageek.common.model.RcvMsgObject;
import cn.mageek.common.model.WebMsgObject;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

import static cn.mageek.common.model.LineType.INT_NUM;
import static cn.mageek.common.model.LineType.SINGLE_ERROR;
import static cn.mageek.common.model.LineType.SINGLE_RIGHT;

/**
 * @author Mageek Chiu
 * @date 2018/5/6 0007:13:49
 */
public class CommandDEL extends Command {

    private static final Logger logger = LoggerFactory.getLogger(CommandDEL.class);

    @Override
    public DataResponse receive(DataRequest dataRequest) {
        String oldValue = this.DATA_POOL.remove(dataRequest.getKey());//
        if (oldValue==null) return new DataResponse(INT_NUM,"0");// 不存在
        return new DataResponse(INT_NUM,"1");// 成功删除
    }

    @Override
    public DataResponse send(WebMsgObject webMsgObject) {
        return null;
    }

}
