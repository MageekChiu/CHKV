package cn.mageek.namenode.main;

import cn.mageek.namenode.res.CommandFactory;
import cn.mageek.namenode.res.CronJobFactory;
import cn.mageek.namenode.service.ClientManager;
import cn.mageek.namenode.service.CronJobManager;
import cn.mageek.namenode.service.DataNodeManager;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
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

    private static final Map<String,Channel> dataNodeMap = new ConcurrentHashMap<>();//管理所有datanode 连接
    private static final Map<String,String> dataNodeClientMap = new ConcurrentHashMap<>();//管理所有datanode 对client开放的IP与端口
//    private static final SortedMap<Integer, String> sortedServerMap = new TreeMap<>();//管理所有datanode 对应 hash 和client开放的IP与端口
    private static final ConcurrentSkipListMap<Integer, String> sortedServerMap =  new ConcurrentSkipListMap<>();//管理所有datanode 对应 hash 和client开放的IP与端口，线程安全
    private static final Map<String,Channel> clientMap = new ConcurrentHashMap<>();//管理所有client 连接



    public static void main(String[] args){
        Thread.currentThread().setName("NameNode");
        Thread dataNodeManager,clientManager,cronJobManager;
        int jobNumber = 2;
        CountDownLatch countDownLatch = new CountDownLatch(jobNumber);

        try{
            // 初始化命令对象
            CommandFactory.construct();
            // 初始化定时任务对象
            CronJobFactory.construct();

            dataNodeManager = new Thread(new DataNodeManager(dataNodeMap,dataNodeClientMap, sortedServerMap,countDownLatch),"DataNodeManager");dataNodeManager.start();
//            clientManager = new Thread(new ClientManager(dataNodeMap,clientMap,countDownLatch),"ClientManager");clientManager.start();
            cronJobManager = new Thread(new CronJobManager(countDownLatch),"CronJobManager");cronJobManager.start();
            countDownLatch.await();//等待其他几个线程完全启动，然后才能对外提供服务
            logger.info("NameNode is fully up now");
        }catch(Exception ex) {
            logger.error("server start error:",ex);//log4j能直接渲染stack trace
            CommandFactory.destruct();
        }
    }
}
