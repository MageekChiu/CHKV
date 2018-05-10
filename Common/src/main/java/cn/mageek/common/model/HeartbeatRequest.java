package cn.mageek.common.model;

import java.io.Serializable;

/**
 * 心跳请求
 * @author Mageek Chiu
 * @date 2018/5/6 0006:13:29
 */
public class HeartbeatRequest implements Serializable {

    private String IPPort;// DataNode自己对客户端开放的的 IP:Port，节点之间迁移数据时可以复用
    private String status;// HeartbeatType ONLINE,OFFLINE,RUNNING
    private long memoryAvailable;// DataNode自己内存剩余，单位 Byte

    public HeartbeatRequest(String IPPort, String status, long memoryAvailable) {
        this.IPPort = IPPort;
        this.status = status;
        this.memoryAvailable = memoryAvailable;
    }

//    public HeartbeatRequest(String IPPort, long memoryAvailable) {
//        this.IPPort = IPPort;
//        this.memoryAvailable = memoryAvailable;
//    }

    public String getIPPort() {
        return IPPort;
    }

    public void setIPPort(String IPPort) {
        this.IPPort = IPPort;
    }

    public long getMemoryAvailable() {
        return memoryAvailable;
    }

    public void setMemoryAvailable(long memoryAvailable) {
        this.memoryAvailable = memoryAvailable;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "HeartbeatRequest -- IPPort:"+IPPort+",memoryAvailable Byte:"+memoryAvailable+",status:"+status;
    }
}
