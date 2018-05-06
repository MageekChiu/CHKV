package cn.mageek.common.command;

import cn.mageek.common.model.DataRequest;
import cn.mageek.common.model.DataResponse;
import cn.mageek.common.model.RcvMsgObject;
import cn.mageek.common.model.WebMsgObject;
import cn.mageek.common.res.CommandFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 抽象策略类
 * @author Mageek Chiu
 * @date 2018/3/7 0007:19:27
 */
public abstract class Command {

    public Map<String,String> DATA_POOL;// 所有command都引用这一个map，而且多线程，所以要用ConcurrentHashMap
    private static final Logger logger = LoggerFactory.getLogger(Command.class);


    // 处理收到来自客户端的消息，返回需要响应的消息，若无则返回null
    public abstract DataResponse receive(DataRequest dataRequest);
    // 根据参数返回发送给客户端的消息
    public abstract DataResponse send(WebMsgObject webMsgObject);
    // 设置数据存储池
    public void setDataPool(Map<String, String> dataPool){
        this.DATA_POOL = dataPool;
//        logger.debug("Command DATA_POOL initialized:{}",DATA_POOL.hashCode());
    }

}
