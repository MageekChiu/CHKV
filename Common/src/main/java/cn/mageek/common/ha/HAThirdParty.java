package cn.mageek.common.ha;

/**
 * @author Mageek Chiu
 * @date 2018/5/21 0021:12:26
 */
public abstract class HAThirdParty implements NameNodeWatcher,NameNodeMaster{

//     第三方组件的连接
//    public Object con;
    // 第三方组件获取连接
    public abstract void getCon();
    // 第三方组件释放连接
    public abstract void releaseCon();

    // master节点
    protected volatile String masterNode;
    // 当前节点
    protected String thisNode;

    // 不一定是即时的
    public String getMasterNode() {
        return masterNode;
    }

    public void setMasterNode(String masterNode) {
        this.masterNode = masterNode;
    }

    public String getThisNode() {
        return thisNode;
    }

    public void setThisNode(String thisNode) {
        this.thisNode = thisNode;
    }
}
