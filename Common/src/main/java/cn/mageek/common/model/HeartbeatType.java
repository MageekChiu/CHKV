package cn.mageek.common.model;

/**
 * @author Mageek Chiu
 * @date 2018/5/8 0008:15:30
 */
public class HeartbeatType {
    public static final String ONLINE = "ONLINE";//发起上线
    public static final String OFFLINE = "OFFLINE";// 发起下线
    public static final String RUNNING = "RUNNING";// 运行中
    public static final String TRANSFERRING = "TRANSFERRING";// 迁移中

//    public static final String OFFTransfering = "OFFTransfering";// 下线迁移中
//    public static final String ONTransfering = "ONTransfering";// 上一个节点上线迁移中
//    public static final String SYNCTransfering = "SYNCTransfering";// 主从复制迁移中
}
