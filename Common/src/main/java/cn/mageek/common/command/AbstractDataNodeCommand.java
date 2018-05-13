package cn.mageek.common.command;

import cn.mageek.common.model.DataRequest;
import cn.mageek.common.model.DataResponse;
import cn.mageek.common.model.WebMsgObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * 服务端抽象命令类，处理不同的请求类型
 * @author Mageek Chiu
 * @date 2018/5/6 0007:16:27
 */
public abstract class AbstractDataNodeCommand {

//    public Map<String,String> DATA_POOL;// 所有command都引用这一个map，而且多线程，所以要用ConcurrentHashMap

    // 设置数据存储池
//    public void setDataPool(Map<String, String> dataPool){
//        this.DATA_POOL = dataPool;
////        logger.debug("AbstractDataNodeCommand DATA_POOL initialized:{}",DATA_POOL.hashCode());
//    }

    // 处理收到来自客户端的消息，返回需要响应的消息，若无则返回null
    public abstract DataResponse receive(DataRequest dataRequest);
    // 根据参数返回发送给客户端的消息
    public abstract DataResponse send(WebMsgObject webMsgObject);

}
