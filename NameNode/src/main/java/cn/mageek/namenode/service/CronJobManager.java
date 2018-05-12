package cn.mageek.namenode.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static cn.mageek.namenode.main.NameNode.countDownLatch;

/**
 * @author Mageek Chiu
 * @date 2018/3/7 0007:20:24
 */
public class CronJobManager implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(CronJobManager.class);
//    private static ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(5);

//    private CountDownLatch countDownLatch;

//    public CronJobManager(CountDownLatch countDownLatch) {
//        this.countDownLatch = countDownLatch;
//    }


    public void run() {

        logger.info("CronJobManager is up now");
        countDownLatch.countDown();
    }

}
