package cn.mageek.common.model;

import java.io.Serializable;

/**
 * @author Mageek Chiu
 * @date 2018/5/6 0006:13:28
 */
public class WatchRequest implements Serializable {
    private boolean immediately ; // 是否立即需要hash环信息，是就直接回复，不是就不回复而是等待有变化再推送

    public WatchRequest(boolean immediately) {
        this.immediately = immediately;
    }

    public boolean isImmediately() {
        return immediately;
    }

    public void setImmediately(boolean immediately) {
        this.immediately = immediately;
    }

    @Override
    public String toString() {
        return "WatchRequest -- immediately:"+immediately;
    }
}
