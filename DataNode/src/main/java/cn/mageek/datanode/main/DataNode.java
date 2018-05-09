package cn.mageek.datanode.main;

import cn.mageek.common.model.HeartbeatType;
import cn.mageek.datanode.jobs.DataTransfer;
import cn.mageek.datanode.jobs.Heartbeat;
import cn.mageek.datanode.res.CommandFactory;
import cn.mageek.datanode.res.JobFactory;
import cn.mageek.datanode.service.DataManager;
import cn.mageek.datanode.service.CronJobManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.Signal;
import sun.misc.SignalHandler;

import java.lang.management.ManagementFactory;
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
    private static volatile Map<String,String> DATA_POOL = new ConcurrentHashMap<>(1024) ;// 被置为null 则意味着节点该下线了

    public static void main(String[] args){
        Thread currentThread = Thread.currentThread();
        currentThread.setName(("DataNode"+Math.random()*100).substring(0,10));
        String threadName = currentThread.getName();
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

            //放入一些数据 做测试
            for (int i = 0 ; i < 22 ; i++ ) DATA_POOL.put(threadName+i,threadName);

            CountDownLatch countDownLatch = new CountDownLatch(jobNumber);
            // n 个线程分别启动 n 个服务
            dataManager = new Thread(new DataManager(countDownLatch),"DataManager");dataManager.start();
            cronJobManager = new Thread(new CronJobManager(countDownLatch),"CronJobManager");cronJobManager.start();
            countDownLatch.await();//等待其他几个线程完全启动，然后才能对外提供服务
            logger.info("DataNode is fully up now, pid:{}",ManagementFactory.getRuntimeMXBean().getName());

            // 监听来自外部的下线消息，收到后立马给NameNode发消息，经过允许和转移数据完成后才能下线也就是把DATA_POOL置为null
//            String offline = System.getenv("DataNodeOffline");
//            // 使用getProperties获得的其实是虚拟机的变量形如： -Djavaxxxx。
//            // getenv方法才是真正的获得系统环境变量，比如Path之类。
//            while (offline==null){
//                offline = System.getenv("DataNodeOffline");// windows: set DataNodeOffline true 但是不是全局，只对当前cmd界面管用，所以不可行
//                Thread.sleep(5000);// 睡5秒再检查
//            }
            Signal.handle(new Signal("ILL"),new MySignalHandler());// 注册信号，但是windows的信号比较麻烦
            while (DATA_POOL != null){
                Thread.sleep(5000);// 睡5秒再检查
            }
            logger.info("DataNode can be safely shutdown now");// 数据转移完成，DATA_POOL == null，可以让运维手动关闭本进程了

        }catch(Exception ex) {
            logger.error("DataNode start error:",ex);
            CommandFactory.destruct();
            JobFactory.destruct();
        }
    }

    static class MySignalHandler implements SignalHandler {
        @Override
        public void handle(Signal signal) {// 收到信号，给nameNode发送请求
//            signal.getName();
            logger.warn("get offline signal");
            Heartbeat heartbeat = (Heartbeat) JobFactory.getJob("Heartbeat");
            heartbeat.run1(HeartbeatType.OFFLINE);
        }
    }


}
