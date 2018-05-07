package cn.mageek.client.service;

import cn.mageek.datanode.res.CronJobFactory;
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
    private static ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(5);

    private CountDownLatch countDownLatch;

    public CronJobManager(CountDownLatch countDownLatch) {
        this.countDownLatch = countDownLatch;
    }


    public void run() {
        scheduledExecutorService.scheduleAtFixedRate(CronJobFactory.getCronJob("CacheToDB"),2,10, TimeUnit.SECONDS);
//        scheduledExecutorService.scheduleAtFixedRate(new OnlineCount(),5,5, TimeUnit.SECONDS);
//        scheduledExecutorService.scheduleAtFixedRate(new OnlineCount(),5,3, TimeUnit.SECONDS);

        scheduledExecutorService.scheduleAtFixedRate(CronJobFactory.getCronJob("OnlineCount"),5,24, TimeUnit.HOURS);

        logger.info("CronJobManager is up now");
        countDownLatch.countDown();
    }

}
