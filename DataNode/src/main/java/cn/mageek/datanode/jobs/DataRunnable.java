package cn.mageek.datanode.jobs;

import java.util.Map;

/**
 * @author Mageek Chiu
 * @date 2018/5/9 0009:10:24
 */
public class DataRunnable implements Runnable {

    protected Map<String,String> DATA_POOL ;// 数据存储池

    @Override
    public void run() {

    }

    /**
     * 传入 数据存储池
     * @param dataPool 数据存储池
     */
    public void connect(Map<String,String> dataPool){
        this.DATA_POOL = dataPool;
    }
}
