package cn.mageek.datanode.cron;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static cn.mageek.datanode.res.ConstPool.L_SUCCESS;


/**
 * 数据迁移
 * @author Mageek Chiu
 * @date 2018/5/7 0007:12:41
 */
public class DataTransfer implements Runnable{
    private static final Logger logger = LoggerFactory.getLogger(DataTransfer.class);

    public void run(){
        logger.info(L_SUCCESS);
        long memoryAvailable = Runtime.getRuntime().freeMemory();
//        long cpuAvailable = Runtime.getRuntime().availableProcessors();

    }
}
