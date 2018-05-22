package cn.mageek.common.ha;

/**
 * @author Mageek Chiu
 * @date 2018/5/21 0021:12:15
 */
public interface NameNodeMaster {

    // 成为master
    boolean becomeMaster();

    // 获得当前即时的master的 ip:port
    String getInstantMaster();

}
