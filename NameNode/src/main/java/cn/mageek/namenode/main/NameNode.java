package cn.mageek.namenode.main;

import cn.mageek.common.model.WatchResponse;
import cn.mageek.namenode.res.CommandFactory;
import cn.mageek.namenode.res.CronJobFactory;
import cn.mageek.namenode.service.ClientManager;
import cn.mageek.namenode.service.CronJobManager;
import cn.mageek.namenode.service.DataNodeManager;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CountDownLatch;

/**
 * 管理本应用的所有服务
 * @author Mageek Chiu
 * @date 2018/3/5 0005:19:26
 */

public class NameNode {

    private static final Logger logger = LoggerFactory.getLogger(NameNode.class);

    // 存储信息
//    key表示dataNode的/ip:port，value表示对应信道 ， 上线就有，并且维护，保证map里面的连接都是活跃的
    public static volatile Map<String,Channel> dataNodeMap = new ConcurrentHashMap<>();//管理所有datanode 连接
//    key表示dataNode的/ip:port，value表示其对客户端开放的ip:port  ， 发送心跳就有
    public static volatile Map<String,String> dataNodeClientMap = new ConcurrentHashMap<>();//管理所有datanode 对client开放的IP与端口
//    key表示dataNode的hash值，value表示其对客户端开放的ip:port  ， 发送心跳状态更新后就有
    public static volatile ConcurrentSkipListMap<Integer, String> sortedServerMap =  new ConcurrentSkipListMap<>();//管理所有datanode 对应 hash 和client开放的IP与端口，线程安全
//    key表示client的/ip:port，value表示对应信道
    public static volatile Map<String,Channel> clientMap = new ConcurrentHashMap<>();//管理所有client 连接

    // 辅助参数
    public static volatile boolean dataNodeChanged = false;// DataNode有无变化
    public static volatile CountDownLatch countDownLatch;//任务个数


    public static void main(String[] args){
        Thread.currentThread().setName("NameNode");

        int jobNumber = 3;countDownLatch = new CountDownLatch(jobNumber);
        Thread dataNodeManager,clientManager,cronJobManager;

        try{
            // 初始化命令对象
            CommandFactory.construct();
            // 初始化定时任务对象
            CronJobFactory.construct();

//            dataNodeManager = new Thread(new DataNodeManager(dataNodeMap,dataNodeClientMap, sortedServerMap,countDownLatch),"DataNodeManager");dataNodeManager.start();
//            clientManager = new Thread(new ClientManager(sortedServerMap,clientMap,countDownLatch),"ClientManager");clientManager.start();
//            cronJobManager = new Thread(new CronJobManager(countDownLatch),"CronJobManager");cronJobManager.start();

            dataNodeManager = new Thread(new DataNodeManager(),"DataNodeManager");dataNodeManager.start();
            clientManager = new Thread(new ClientManager(),"ClientManager");clientManager.start();
            cronJobManager = new Thread(new CronJobManager(),"CronJobManager");cronJobManager.start();

            countDownLatch.await();//等待其他几个线程完全启动，然后才能对外提供服务
            logger.info("NameNode is fully up now ,jobNumber :{},pid:{}",jobNumber,ManagementFactory.getRuntimeMXBean().getName());
            // 开始监控DataNode变更事件
            dataNodeWatcher();

        }catch(Exception ex) {
            logger.error("NameNode start error:",ex);
            CommandFactory.destruct();
            CronJobFactory.destruct();
        }
    }

    private static void dataNodeWatcher() throws InterruptedException {
        //noinspection InfiniteLoopStatement
        for (;;){
            if (dataNodeChanged){
                dataNodeChanged = false;
                logger.info("DataNode dataNodeChanged,now {},\r\n client:{}",sortedServerMap,clientMap);
                WatchResponse watchResponse = new WatchResponse(sortedServerMap);
                clientMap.forEach((k,v)->{
                    v.writeAndFlush(watchResponse);
                });
            }
            Thread.sleep(5000);// 每隔5秒检测一次是否有变化
        }
    }
}
