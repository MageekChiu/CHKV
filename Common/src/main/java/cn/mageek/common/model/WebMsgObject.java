package cn.mageek.common.model;

import java.util.Map;

/**
 * web传递的的消息对象
 * @author Mageek Chiu
 * @date 2018/3/8 0008:20:14
 */
public class WebMsgObject {
    /**
     * 客户端标识
     */
    private String clientId;
    /**
     * 控制码
     */
    private String command;
    /**
     * 所需参数
     */
    private Map<String,String> data;
    /**
     * 推送标识
     */
    private String ref;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public Map<String, String> getData() {
        return data;
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    @Override
    public String toString() {
        return "\tclientId:" + clientId + "\n" +
                "\tcommand:" + command + "\n" +
                "\tref:" + ref + "\n" ;
    }
}
