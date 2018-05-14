package cn.mageek.datanode.service;

import cn.mageek.datanode.res.JobFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static cn.mageek.datanode.main.DataNode.countDownLatch;

/**
 * @author Mageek Chiu
 * @date 2018/3/7 0007:20:24
 */
public class CronJobManager implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(CronJobManager.class);
    // 如果任务执行过程中抛出了异常，那么过ScheduledExecutorService就会停止执行任务，且也不会再周期地执行该任务了。所以你如果想保住任务，那么要在任务里面catch住一切可能的异常。
    private static ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

    public void run() {
        // 定时向namenode发起心跳
        scheduledExecutorService.scheduleAtFixedRate( JobFactory.getJob("Heartbeat"),2,10, TimeUnit.SECONDS);// 固定每隔多少时间执行一下任务，但是上一个任务结束才会执行下一个，保证任务执行的频率
//        scheduledExecutorService.scheduleWithFixedDelay( JobFactory.getJob("Heartbeat"),2,10, TimeUnit.SECONDS);// 上一个任务结束后延迟delay时间执行下一个任务，保证任务执行的间隔

        logger.info("CronJobManager is up now");
        countDownLatch.countDown();
    }

}
