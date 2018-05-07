package cn.mageek.datanode.main;

import cn.mageek.datanode.res.CommandFactory;
import cn.mageek.datanode.res.CronJobFactory;
import cn.mageek.datanode.service.ClientManager;
import cn.mageek.datanode.service.CronJobManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * 管理本应用的所有服务
 * redis-cli -h 127.0.0.1 -p 10100
 * @author Mageek Chiu
 * @date 2018/3/5 0005:19:26
 */

public class DataNode {

    private static final Logger logger = LoggerFactory.getLogger(DataNode.class);
    private static volatile Map<String,String> DATA_POOL ;//实际数据存储

    public static void main(String[] args){
        Thread.currentThread().setName("DataNode");
        Thread clientManager,cronJobManager;
        int jobNumber = 2;
        try{
            // 初始化命令对象
            DATA_POOL = new ConcurrentHashMap<>(1024);
//            DATA_POOL.put("clientPort",clientPort);// 有了这一句下面才是DATA_POOL:1132277150,1。否则就是 DATA_POOL:0,0
//            logger.debug("DATA_POOL:{},{}",DATA_POOL.hashCode(),DATA_POOL.size());
            CommandFactory.construct(DATA_POOL);// 所有command都是单例对象，共享这一个数据池（ConcurrentHashMap）

            // 初始化定时任务对象
            CronJobFactory.construct();

            CountDownLatch countDownLatch = new CountDownLatch(jobNumber);
            // n 个线程分别启动 n 个服务
            clientManager = new Thread(new ClientManager(countDownLatch),"ClientManager");clientManager.start();
            cronJobManager = new Thread(new CronJobManager(countDownLatch),"CronJobManager");cronJobManager.start();
            countDownLatch.await();//等待其他几个线程完全启动，然后才能对外提供服务
            logger.info("DataNode is fully up now");
        }catch(Exception ex) {
            logger.error("DataNode start error:",ex);
            CommandFactory.destruct();
            CronJobFactory.destruct();
        }
    }
}
