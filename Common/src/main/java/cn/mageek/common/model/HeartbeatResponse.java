package cn.mageek.common.model;

import java.io.Serializable;

/**
 * 心跳响应
 * @author Mageek Chiu
 * @date 2018/5/6 0006:13:29
 */
public class HeartbeatResponse implements Serializable {

    private boolean ok ; // 相应状态
    private int dataNodeNumber ;// datanode数量
    private String IPPort ; // 若不为 null 则需要转移数据到另一个节点，迁移方式采用与客户端相同的做法，但是一次打包多个数据，减小网络传输次数

    public HeartbeatResponse(boolean ok, int dataNodeNumber, String IPPort) {
        this.ok = ok;
        this.dataNodeNumber = dataNodeNumber;
        this.IPPort = IPPort;
    }

    public boolean isOk() {
        return ok;
    }

    public void setOk(boolean ok) {
        this.ok = ok;
    }

    public int getDataNodeNumber() {
        return dataNodeNumber;
    }

    public void setDataNodeNumber(int dataNodeNumber) {
        this.dataNodeNumber = dataNodeNumber;
    }

    public String getIPPort() {
        return IPPort;
    }

    public void setIPPort(String IPPort) {
        this.IPPort = IPPort;
    }

    @Override
    public String toString() {
        return "HeartbeatResponse -- ok:"+ok+",dataNodeNumber :"+dataNodeNumber+",IPPort"+IPPort;
    }
}
