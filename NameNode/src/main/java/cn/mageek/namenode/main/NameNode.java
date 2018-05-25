package cn.mageek.namenode.main;

import cn.mageek.common.ha.HAThirdParty;
import cn.mageek.common.ha.ZKThirdParty;
import cn.mageek.common.model.WatchResponse;
import cn.mageek.namenode.res.CommandFactory;
import cn.mageek.namenode.res.CronJobFactory;
import cn.mageek.namenode.service.ClientManager;
import cn.mageek.namenode.service.CronJobManager;
import cn.mageek.namenode.service.DataNodeManager;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

import static cn.mageek.common.util.HAHelper.getNodeString;
import static cn.mageek.common.util.PropertyLoader.load;
import static cn.mageek.common.util.PropertyLoader.loadWorkThread;

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
    public static volatile Map<String,String> dataNodeClientMap = new ConcurrentHashMap<>();//管理所有datanode 及其对client开放的IP与端口
//    key表示dataNode的hash值，value表示其对客户端开放的ip:port  ， 发送心跳状态更新后就有
    public static volatile ConcurrentSkipListMap<Integer, String> sortedServerMap =  new ConcurrentSkipListMap<>();//管理所有datanode 对应 hash 和client开放的IP与端口
//    key表示client的/ip:port，value表示对应信道
    public static volatile Map<String,Channel> clientMap = new ConcurrentHashMap<>();//管理所有client 连接

    // 辅助参数
    public static volatile boolean dataNodeChanged = false;// DataNode有无变化
    public static volatile CountDownLatch countDownLatch;//任务个数

    // 配置信息
    public static String dataNodePort;
    public static int dataNodeThread;
    public static String clientPort;
    public static int clientThread;

    // HA 信息
    private static boolean useHA;
    private static String thisNode;
    private static String masterNode;
    private static String connectAddr ;
    private static int sessionTimeout;
    private static int connectionTimeout;
    private static String masterNodePath ;
    private static int baseSleepTimeMs ;
    private static int maxRetries ;


    static {
        try( InputStream in = ClassLoader.class.getResourceAsStream("/app.properties")) {
            Properties pop = new Properties();
            pop.load(in);
            dataNodePort = load(pop,"namenode.datanode.port");// 对dataNode开放的端口
            dataNodeThread = loadWorkThread(pop,"namenode.datanode.workThread");
            clientPort = load(pop,"namenode.client.port");// 对client开放的端口
            clientThread = loadWorkThread(pop,"namenode.client.workThread");
            logger.debug("config dataNodePort:{},dataNodeThread:{},clientPort:{},clientThread:{}", dataNodePort,dataNodeThread,clientPort,clientThread);

            useHA = Boolean.parseBoolean(load(pop,"namenode.useHA"));
            if (useHA){
                logger.info("using HA");
                String dataNodeIP = load(pop,"namenode.datanode.ip");
                String clientIP = load(pop,"namenode.client.ip");
                thisNode =  getNodeString(dataNodeIP,dataNodePort,clientIP,clientPort);

                connectAddr = load(pop,"namenode.zk.connectAddr");//
                sessionTimeout = Integer.parseInt(load(pop,"namenode.zk.sessionTimeout")); //
                connectionTimeout = Integer.parseInt(load(pop,"namenode.zk.connectionTimeout")); //
                masterNodePath = load(pop,"namenode.zk.masterNodePath"); //
                baseSleepTimeMs = Integer.parseInt(load(pop,"namenode.zk.baseSleepTimeMs")); //
                maxRetries = Integer.parseInt(load(pop,"namenode.zk.maxRetries")); //
                logger.debug("config connectAddr:{},sessionTimeout:{},connectionTimeout{},masterNodePath:{},baseSleepTimeMs:{},maxRetries:{}", connectAddr,sessionTimeout,connectionTimeout,masterNodePath,baseSleepTimeMs,maxRetries);
            }else {
                logger.info("not using HA");
            }
        } catch (Exception e) {
            logger.error("read config error",e);
        }
    }

    public static void main(String[] args){
        Thread.currentThread().setName("NameNode");

        int jobNumber = 3;countDownLatch = new CountDownLatch(jobNumber);
        Thread dataNodeManager,clientManager,cronJobManager;

        try{
            // 初始化命令对象
            CommandFactory.construct();
            // 初始化定时任务对象
            CronJobFactory.construct();

            dataNodeManager = new Thread(new DataNodeManager(),"DataNodeManager");dataNodeManager.start();
            clientManager = new Thread(new ClientManager(),"ClientManager");clientManager.start();
            cronJobManager = new Thread(new CronJobManager(),"CronJobManager");cronJobManager.start();

            countDownLatch.await();//等待其他几个线程完全启动，然后才能对外提供服务
            logger.info("NameNode is fully up now ,jobNumber :{},pid:{}",jobNumber,ManagementFactory.getRuntimeMXBean().getName());

            //HA相关
            if (useHA){
                HAThirdParty party = new ZKThirdParty(connectAddr,sessionTimeout,connectionTimeout,masterNodePath,baseSleepTimeMs,maxRetries);
                nameNodeHA(party);
            }

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
                clientMap.forEach((k,v)-> v.writeAndFlush(watchResponse));
            }
            Thread.sleep(5000);// 每隔5秒检测一次是否有变化
        }
    }

    private static void nameNodeHA(HAThirdParty party ){

        // 下面代码都与具体HA实现无关，能够复用
        party.setThisNode(thisNode);
        boolean result = party.becomeMaster();
        if (result){
            logger.info("Successfully Became Master");
        }else {
            logger.info("Failed to Became Master");
        }
        masterNode = party.getInstantMaster();
        boolean result1 =  thisNode.equals(masterNode);
        if (result1){
            logger.info("Confirmed, I am the Master,masterNode;{}",masterNode);
        }else {
            logger.info("Confirmed,I am the Standby,masterNode;{}",masterNode);
        }

        Consumer<String> consumer = s -> {
            if (s==null){
                logger.error("master NameNode is down, try to become Master");
                if (party.becomeMaster()){
                    logger.info("Successfully tried to Became Master");
                }else {
                    logger.info("Failed to try to Became Master");
                }
            }else{
                masterNode = s;
                logger.info("masterNode may changed:{}",masterNode);
            }
        };
        party.beginWatch(consumer);
    }
}
