package cn.mageek.datanode.main;

import cn.mageek.common.ha.HAThirdParty;
import cn.mageek.common.ha.ZKThirdParty;
import cn.mageek.common.model.HeartbeatType;
import cn.mageek.common.util.HAHelper;
import cn.mageek.datanode.job.DataRunnable;
import cn.mageek.datanode.job.Heartbeat;
import cn.mageek.datanode.res.CommandFactory;
import cn.mageek.datanode.res.JobFactory;
import cn.mageek.datanode.service.DataManager;
import cn.mageek.datanode.service.CronJobManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

import static cn.mageek.common.util.PropertyLoader.load;
import static com.sun.jmx.remote.internal.IIOPHelper.connect;

/**
 * 管理本应用的所有服务
 * redis-cli -h 127.0.0.1 -p 10100
 * @author Mageek Chiu
 * @date 2018/3/5 0005:19:26
 */

public class DataNode {

    private static final Logger logger = LoggerFactory.getLogger(DataNode.class);
    private static final String pid = ManagementFactory.getRuntimeMXBean().getName();

    private static int offlinePort = 6666;// 默认值，可调
    private static String offlineCmd = "k";

    public static String nameNodeIP ;
    public static String nameNodePort ;
    public static String clientPort;
    public static String clientIP;

    // HA 信息
    private static boolean useHA;
    private static String connectAddr ;
    private static int sessionTimeout;
    private static int connectionTimeout;
    private static String masterNodePath ;
    private static int baseSleepTimeMs ;
    private static int maxRetries ;

    //本节点的数据存储，ConcurrentHashMap 访问效率高于 ConcurrentSkipListMap，但是转移数据时就需要遍历而不能直接排序了，考虑到转移数据情况并不多，访问次数远大于转移次数，所以就不用ConcurrentSkipListMap
    public static volatile Map<String,String> DATA_POOL = new ConcurrentHashMap<>(1024) ;//被置为null 则意味着节点该下线了
    public static volatile Map<String,Long> DATA_EXPIRE = new ConcurrentHashMap<>(1024);// 键-失效时间的秒表示
    public static volatile CountDownLatch countDownLatch;//任务个数

    public static void main(String[] args){
        Thread currentThread = Thread.currentThread();
        currentThread.setName(("DataNode"+Math.random()*100).substring(0,10));
        String threadName = currentThread.getName();
        logger.debug("current thread {}" ,threadName);

        Thread dataManager,cronJobManager;
        int jobNumber = 2;countDownLatch = new CountDownLatch(jobNumber);

        try(InputStream in = ClassLoader.class.getResourceAsStream("/app.properties")){
            Properties pop = new Properties(); pop.load(in);
            offlinePort = Integer.parseInt(load(pop,"datanode.offline.port")); //下线监听端口
            offlineCmd = load(pop,"datanode.offline.cmd"); //下线命令字
            logger.debug("config offlinePort:{},offlineCmd:{}", offlinePort,offlineCmd);

            nameNodeIP = load(pop,"datanode.namenode.ip");// nameNode 对DataNode开放心跳IP
            nameNodePort = load(pop,"datanode.namenode.port");// nameNode 对DataNode开放心跳Port
            clientIP = load(pop,"datanode.client.ip");//dataNode对client开放的ip
            clientPort = load(pop,"datanode.client.port");//dataNode对client开放的端口
            logger.debug("Heartbeat config nameNodeIP:{},nameNodePort:{},clientIP:{},clientPort:{}", nameNodeIP, nameNodePort,clientIP,clientPort);

            useHA = Boolean.parseBoolean(load(pop,"databode.useHA"));
            if (useHA) {
                logger.info("using HA");
                connectAddr = load(pop, "datanode.zk.connectAddr");//
                sessionTimeout = Integer.parseInt(load(pop, "datanode.zk.sessionTimeout")); //
                connectionTimeout = Integer.parseInt(load(pop, "datanode.zk.connectionTimeout")); //
                masterNodePath = load(pop, "datanode.zk.masterNodePath"); //
                baseSleepTimeMs = Integer.parseInt(load(pop, "datanode.zk.baseSleepTimeMs")); //
                maxRetries = Integer.parseInt(load(pop, "datanode.zk.maxRetries")); //
            }

            // 初始化命令对象,所有command都是单例对象
            CommandFactory.construct();

            // 初始化任务对象
            JobFactory.construct();

            //放入一些数据 做测试
            for (int i = 1 ; i <= 25 ; i++ ) DATA_POOL.put(threadName+i,threadName);

            // n 个线程分别启动 n 个服务
            dataManager = new Thread(new DataManager(),"DataManager");dataManager.start();
            cronJobManager = new Thread(new CronJobManager(),"CronJobManager");cronJobManager.start();

            //等待其他几个线程完全启动，然后才能对外提供服务
            countDownLatch.await();
            logger.info("DataNode is fully up now, pid:{}",pid);

            if (useHA){
                HAThirdParty party = new ZKThirdParty(connectAddr,sessionTimeout,connectionTimeout,masterNodePath,baseSleepTimeMs,maxRetries);
                dataNodeHA(party);
            }

            // 开启socket，这样就能用telnet的方式来发送下线命令了
            signalHandler();

        }catch(Exception ex) {
            logger.error("DataNode start error:",ex);
            CommandFactory.destruct();
            JobFactory.destruct();
        }
    }

    private static void signalHandler() {
        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(offlinePort);// 监听信号端口，telnet 127.0.0.1 6666 。 输入字母 k 按回车就行
            for (;;) {
                //接收客户端连接的socket对象
                try (Socket connection = serverSocket.accept()) {// 使用这个方式，try结束后会自动断开连接
                    //接收客户端传过来的数据，会阻塞
                    BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String msg = br.readLine();
                    logger.info("signalHandler received msg: {}",msg );
                    Writer writer = new OutputStreamWriter(connection.getOutputStream());
                    if (offlineCmd.equals(msg)){
                        writer.append("going down");writer.flush();writer.close();
                        dataTransfer();
                        break;// 可以下线了，所以也不必监听这个端口了
                    }else{
                        writer.append("bad option");writer.flush();writer.close();
                    }
                } catch (Exception e) {
                    logger.error("signalHandler connection error", e);
                }
            }
        } catch (IOException e) {
            logger.error("signalHandler ServerSocket error",e);
        }
    }

    /**
     * 收到下线信号，先转移数据，阻塞至数据转移完成
     */
    private static void dataTransfer() throws InterruptedException {
        logger.warn("get offline signal");
        Heartbeat heartbeat = (Heartbeat) JobFactory.getJob("Heartbeat");
        heartbeat.run1(HeartbeatType.OFFLINE);// heartbeat对象的连接早已打开并且由定时任务一直保持着，所以主线程直接发起下线请示与数据转移工作

        while (DATA_POOL != null){// 依然在线
            Thread.sleep(5000);// 睡5秒再检查
            logger.debug("waiting for dataTransfer to complete");
        }
        // 数据已转移完毕并清空，可以下线
        logger.info("DataNode can be safely shutdown now,{}",pid);// DATA_POOL == null，数据转移完成，可以让运维手动关闭本进程了
    }

    private static void dataNodeHA(HAThirdParty party ){

        party.getInstantMaster();
        Consumer<String> consumer = s -> {
            if (s==null){
                logger.error("masterNode is down, waiting");
            }else{
                logger.info("masterNode may have changed:{}",s);
                HAHelper helper = new HAHelper(s);
                String thisNameNodeIP = helper.getDataNodeIP();
                String thisNameNodePort = helper.getDataNodePort();

                Heartbeat heartbeat = (Heartbeat) JobFactory.getJob("Heartbeat");

                if (!(thisNameNodeIP.equals(nameNodeIP)&&thisNameNodePort.equals(nameNodePort))){// 不同，那肯定需要重连
                    logger.info("masterNode indeed have changed,reconnecting");
                    heartbeat.disconnect();// 可能已经断掉了，但是加一下确保
                    nameNodeIP = thisNameNodeIP;nameNodePort = thisNameNodePort;
//                    heartbeat.connect();// 不需要在这里连接 定时任务自己会去连接
                }else {// 相同，有可能断线后又上线
                    if (!heartbeat.isConnected()){//断线了就重连
                        heartbeat.disconnect();
//                        heartbeat.connect();// 不需要在这里连接 定时任务自己会去连接
                    }
                    // 否则就没断线，只是nameNode和高可用注册中心连接抖动
                }
            }
        };

        party.beginWatch(consumer);
        while (party.getMasterNode()==null);//忙等待
        logger.debug("present master NameNode:{}",party.getMasterNode());

    }

}
