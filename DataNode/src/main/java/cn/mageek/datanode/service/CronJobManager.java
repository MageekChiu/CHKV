package cn.mageek.datanode.service;

import cn.mageek.datanode.res.CommandFactory;
import cn.mageek.datanode.res.JobFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static cn.mageek.common.util.PropertyLoader.load;
import static cn.mageek.datanode.main.DataNode.countDownLatch;

/**
 * @author Mageek Chiu
 * @date 2018/3/7 0007:20:24
 */
public class CronJobManager implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(CronJobManager.class);
    // 如果任务执行过程中抛出了异常，那么过ScheduledExecutorService就会停止执行任务，且也不会再周期地执行该任务了。所以你如果想保住任务，那么要在任务里面catch住一切可能的异常。
    private static ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2);

    private static int heartbeat = 10;
    private static int expireChecking = -1;


    static {
        try (InputStream in = ClassLoader.class.getResourceAsStream("/app.properties")){
            Properties pop = new Properties();
            pop.load(in);
            expireChecking = Integer.parseInt(load(pop, "datanode.interval.expireChecking"));
            heartbeat = Integer.parseInt(load(pop, "datanode.interval.heartbeat"));
            logger.debug("config interval heartbeat:{},expireChecking:{}", heartbeat, expireChecking);
        } catch (Exception ex) {
            logger.error("tart error:", ex);
            CommandFactory.destruct();
            JobFactory.destruct();
        }
    }

    public void run() {
        // 定时向namenode发起心跳
        scheduledExecutorService.scheduleAtFixedRate( JobFactory.getJob("Heartbeat"),1,heartbeat, TimeUnit.SECONDS);// 固定每隔多少时间执行一下任务，但是上一个任务结束才会执行下一个，保证任务执行的频率
//        scheduledExecutorService.scheduleWithFixedDelay( JobFactory.getJob("Heartbeat"),2,10, TimeUnit.SECONDS);// 上一个任务结束后延迟delay时间执行下一个任务，保证任务执行的间隔

        // 定时检查过期键
        if (expireChecking>0)
            scheduledExecutorService.scheduleAtFixedRate( JobFactory.getJob("ExpireChecking"),2,expireChecking, TimeUnit.SECONDS);//


        logger.info("CronJobManager is up now，heartbeat：{}，expireChecking：{}",heartbeat,expireChecking);
        countDownLatch.countDown();
    }

}
