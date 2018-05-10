package cn.mageek.common.model;

import java.io.Serializable;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * @author Mageek Chiu
 * @date 2018/5/6 0006:13:28
 */
public class WatchResponse implements Serializable {
    private ConcurrentSkipListMap<Integer, String> hashCircle;// DataNode 构成的环

    public WatchResponse(ConcurrentSkipListMap<Integer, String> hashCircle) {
        this.hashCircle = hashCircle;
    }

    public ConcurrentSkipListMap<Integer, String> getHashCircle() {
        return hashCircle;
    }

    public void setHashCircle(ConcurrentSkipListMap<Integer, String> hashCircle) {
        this.hashCircle = hashCircle;
    }

    @Override
    public String toString() {
        return "WatchResponse -- hashCircle:"+hashCircle;
    }
}
