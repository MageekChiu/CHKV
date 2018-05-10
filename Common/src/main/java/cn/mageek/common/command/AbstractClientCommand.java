package cn.mageek.common.command;

import cn.mageek.common.model.DataRequest;
import cn.mageek.common.model.DataResponse;
import cn.mageek.common.model.WebMsgObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * client抽象命令类，处理不同的请求类型
 * @author Mageek Chiu
 * @date 2018/5/10 0007:19:27
 */
public abstract class AbstractClientCommand {

    // client 解析 收到的 data response
    public abstract void receive(DataResponse response);

    // client 根据参数合成 DataRequest
    public abstract DataRequest send(String CMD,String... args);


}
