package cn.mageek.datanode.main;

import cn.mageek.common.res.CommandFactory;
import cn.mageek.common.res.CronJobFactory;
import cn.mageek.datanode.service.ClientManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
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
        Thread clientManager,heartbeatManager,cronJobManager;
        try( InputStream in = ClassLoader.class.getResourceAsStream("/app.properties")
            ){
            // 读取TCP配置
            Properties pop = new Properties();
            pop.load(in);
            String clientPort = pop.getProperty("datanode.client.port");
            String heartbeatPort = pop.getProperty("datanode.heartbeat.port");
            logger.debug("config clientPort:{},heartbeatPort:{}",clientPort,heartbeatPort);

            // 初始化命令对象
            DATA_POOL = new ConcurrentHashMap<>(1024);
//            DATA_POOL.put("clientPort",clientPort);// 有了这一句下面才是DATA_POOL:1132277150,1。否则就是 DATA_POOL:0,0
//            logger.debug("DATA_POOL:{},{}",DATA_POOL.hashCode(),DATA_POOL.size());
            CommandFactory.construct(DATA_POOL);
            // 初始化定时任务对象
//            CronJobFactory.construct();

            CountDownLatch countDownLatch = new CountDownLatch(1);
            // 三个线程分别启动3个服务 连接管理服务、web消息监听服务、定时任务管理服务
            clientManager = new Thread(new ClientManager(clientPort,countDownLatch),"ClientManager");clientManager.start();
//            heartbeatManager = new Thread(new HeartbeatManager(heartbeatPort,countDownLatch),"HeartbeatManager");webJobManager.start()
//            cronJobManager = new Thread(new CronJobManager(countDownLatch),"CronJobManager");cronJobManager.start();
            countDownLatch.await();//等待其他几个线程完全启动，然后才能对外提供服务
            logger.info("DataNode is fully up now");
        }catch(Exception ex) {
            logger.error("DataNode start error:",ex);
            CommandFactory.destruct();
        }
    }
}
