package cn.mageek.common.ha;

import java.util.function.Consumer;

/**
 * @author Mageek Chiu
 * @date 2018/5/21 0021:12:09
 */
public interface NameNodeWatcher {

    // 开始监听NameNode,发生变化时调用回调函数，能获得最新的 ip:port
    void beginWatch(Consumer<String> nameNodeChanged);

}
