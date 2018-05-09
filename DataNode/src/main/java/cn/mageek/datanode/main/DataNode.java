package cn.mageek.datanode.main;

import cn.mageek.datanode.res.CommandFactory;
import cn.mageek.datanode.res.JobFactory;
import cn.mageek.datanode.service.DataManager;
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
    //本节点的数据存储，ConcurrentHashMap 访问效率高于 ConcurrentSkipListMap，但是转移数据时就需要遍历而不能直接排序了，考虑到转移数据情况并不多，访问次数远大于转移次数，所以就不用ConcurrentSkipListMap
    private static final Map<String,String> DATA_POOL = new ConcurrentHashMap<>(1024) ;

    public static void main(String[] args){
        Thread.currentThread().setName(("DataNode"+Math.random()*100).substring(0,10));
        String threadName = Thread.currentThread().getName();
        logger.debug("current thread {}" ,threadName);
        Thread dataManager,cronJobManager;
        int jobNumber = 2;
        try{
            // 初始化命令对象
//            DATA_POOL.put("clientPort",clientPort);// 有了这一句下面才是DATA_POOL:1132277150,1。否则就是 DATA_POOL:0,0
//            logger.debug("DATA_POOL:{},{}",DATA_POOL.hashCode(),DATA_POOL.size());
            CommandFactory.construct(DATA_POOL);// 所有command都是单例对象，共享这一个数据池（ConcurrentHashMap）

            // 初始化任务对象
            JobFactory.construct(DATA_POOL);// 任务也可能用到数据池

            for (int i = 0 ; i < 22 ; i++ ) DATA_POOL.put(threadName+i,threadName);//放入一些数据

            CountDownLatch countDownLatch = new CountDownLatch(jobNumber);
            // n 个线程分别启动 n 个服务
            dataManager = new Thread(new DataManager(countDownLatch),"DataManager");dataManager.start();
            cronJobManager = new Thread(new CronJobManager(countDownLatch),"CronJobManager");cronJobManager.start();
            countDownLatch.await();//等待其他几个线程完全启动，然后才能对外提供服务
            logger.info("DataNode is fully up now");
        }catch(Exception ex) {
            logger.error("DataNode start error:",ex);
            CommandFactory.destruct();
            JobFactory.destruct();
        }
    }
}
