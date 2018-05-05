package cn.mageek.common.model;

import java.util.Map;

/**
 * net传递的消息对象
 * @author Mageek Chiu
 * @date 2018/3/8 0008:20:15
 */
public class NetMsgObject {
    /**
     * 客户端标识
     */
    private String clientId;
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
                "\tref:" + ref + "\n" ;
    }
}
