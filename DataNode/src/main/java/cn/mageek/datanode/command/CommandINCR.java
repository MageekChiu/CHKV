package cn.mageek.datanode.command;

import cn.mageek.common.command.AbstractDataNodeCommand;
import cn.mageek.common.model.DataRequest;
import cn.mageek.common.model.DataResponse;
import cn.mageek.common.model.LineType;
import cn.mageek.common.model.WebMsgObject;

import java.util.concurrent.atomic.AtomicLong;

import static cn.mageek.datanode.main.DataNode.DATA_POOL;

/**
 * 单增 用CAS保证原子性
 * 这种当前操作依赖于之前值的命令都要这样保证
 * @author Mageek Chiu
 * @date 2018/5/6 0007:13:49
 */
public class CommandINCR extends AbstractDataNodeCommand {

//    private static final Logger logger = LoggerFactory.getLogger(CommandSET.class);

    @Override
    public DataResponse receive(DataRequest dataRequest) {
        String key = dataRequest.getKey();

        if (DATA_POOL.putIfAbsent(key,"1")==null){// 之前不存在
            return new DataResponse(LineType.INT_NUM, "1");
        }

        String oldValue;
        int newValue;
        do {// 之前存在
            oldValue = DATA_POOL.get(key);
            newValue = Integer.parseInt(oldValue) + 1;
        } while (!DATA_POOL.replace(key, oldValue, String.valueOf(newValue)));

        return new DataResponse(LineType.INT_NUM, String.valueOf(newValue));
    }

    @Override
    public DataResponse send(WebMsgObject webMsgObject) {
        return null;
    }

}
