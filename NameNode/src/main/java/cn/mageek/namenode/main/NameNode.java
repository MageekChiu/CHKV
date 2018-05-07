package cn.mageek.namenode.main;

import cn.mageek.namenode.res.CommandFactory;
import cn.mageek.namenode.res.CronJobFactory;
import cn.mageek.namenode.service.ClientManager;
import cn.mageek.namenode.service.CronJobManager;
import cn.mageek.namenode.service.DataNodeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

/**
 * 管理本应用的所有服务
 * @author Mageek Chiu
 * @date 2018/3/5 0005:19:26
 */

public class NameNode {

    private static final Logger logger = LoggerFactory.getLogger(NameNode.class);

    public static void main(String[] args){
        Thread.currentThread().setName("NameNode");
        Thread dataNodeManager,cronJobManager;
        int jobNumber = 2;

        try{
            // 初始化命令对象
            CommandFactory.construct();
            // 初始化定时任务对象
            CronJobFactory.construct();

            CountDownLatch countDownLatch = new CountDownLatch(jobNumber);
            // 三个线程分别启动3个服务 连接管理服务、web消息监听服务、定时任务管理服务
            dataNodeManager = new Thread(new DataNodeManager(countDownLatch),"DataNodeManager");dataNodeManager.start();
            cronJobManager = new Thread(new CronJobManager(countDownLatch),"CronJobManager");cronJobManager.start();
            countDownLatch.await();//等待其他几个线程完全启动，然后才能对外提供服务
            logger.info("NameNode is fully up now");
        }catch(Exception ex) {
            logger.error("server start error:",ex);//log4j能直接渲染stack trace
            CommandFactory.destruct();
        }
    }
}
