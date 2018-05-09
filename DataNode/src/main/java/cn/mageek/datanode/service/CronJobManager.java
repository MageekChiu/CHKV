package cn.mageek.datanode.service;

import cn.mageek.datanode.res.JobFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Mageek Chiu
 * @date 2018/3/7 0007:20:24
 */
public class CronJobManager implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(CronJobManager.class);
    private static ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2);

    private CountDownLatch countDownLatch;

    public CronJobManager(CountDownLatch countDownLatch) {
        this.countDownLatch = countDownLatch;
    }


    public void run() {
        // 定时向namenode发起心跳
        scheduledExecutorService.scheduleAtFixedRate( JobFactory.getJob("Heartbeat"),2,10, TimeUnit.SECONDS);

        logger.info("CronJobManager is up now");
        countDownLatch.countDown();
    }

}
