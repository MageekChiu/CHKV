package cn.mageek.datanode.service;

import cn.mageek.datanode.job.DataRunnable;
import cn.mageek.datanode.job.MSSync;
import cn.mageek.datanode.res.CommandFactory;
import cn.mageek.datanode.res.JobFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;
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
    private static ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(3);

    private static int heartbeat = 10;
    private static int expireChecking = -1;
    private static int sync = 10;

    public static boolean useHA = false;
    private static String slaveIPPort = "";
    private static String vip = "";
    public static volatile boolean isMaster = false;


    static {
        try (InputStream in = ClassLoader.class.getResourceAsStream("/app.properties")){
            Properties pop = new Properties();
            pop.load(in);
            expireChecking = Integer.parseInt(load(pop, "datanode.interval.expireChecking"));
            heartbeat = Integer.parseInt(load(pop, "datanode.interval.heartbeat"));
            useHA = Boolean.parseBoolean(load(pop, "datanode.useHA"));
            if (useHA){
                sync = Integer.parseInt(load(pop, "datanode.interval.sync"));
                heartbeat = Integer.parseInt(load(pop, "datanode.interval.heartbeat"));
                isMaster = Boolean.parseBoolean(load(pop, "datanode.isMaster"));
                slaveIPPort = load(pop, "datanode.slave");
                vip = load(pop, "datanode.vip");

            }
            logger.debug("config interval heartbeat:{},expireChecking:{},useHA:{},isMaster:{},slaveIPPort:{},sync:{}", heartbeat, expireChecking,useHA,isMaster,slaveIPPort,sync);
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

        // 定时同步到从节点
        MSSync msSync = (MSSync) JobFactory.getJob("MSSync");
        msSync.connect(slaveIPPort);
        scheduledExecutorService.scheduleAtFixedRate( msSync,15,sync, TimeUnit.SECONDS);

        // 定时检查过期键
        if (expireChecking>0)// 大于0 才使用定期检查，否则就是 lazy delete
            scheduledExecutorService.scheduleAtFixedRate( JobFactory.getJob("ExpireChecking"),2,expireChecking, TimeUnit.SECONDS);//

        logger.info("CronJobManager is up now");
        countDownLatch.countDown();

        // 定期检查自己是否被keepalived分配了vip
        if (useHA){
            for (;;){
                try {
                    InetAddress thisIP = null ;
                    // 遍历所有的网络接口
                    for (Enumeration ifaces = NetworkInterface.getNetworkInterfaces();ifaces.hasMoreElements();){
                        NetworkInterface iface = (NetworkInterface) ifaces.nextElement();
                        // 在所有的接口下再遍历IP，如果找到vip就break
                        for (Enumeration inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements(); ) {
                            InetAddress inetAddr = (InetAddress) inetAddrs.nextElement();
                            if (inetAddr.isSiteLocalAddress() && inetAddr.toString().substring(1).equals(vip)) {//
                                thisIP = inetAddr;
                                break;
                            }
                        }
                    }
                    if(thisIP != null){// 找到vip
                        if (isMaster){// 继续运行
                            logger.debug("continue running");
                        }else{// 备机该上线了
                            logger.info("slave is up");
                            isMaster = true;
                        }
                    }else {// 没找到
                        if (isMaster){// 主机宕机
                            logger.error("master is down");
                            isMaster = false;
                        }else{// 是备机

                        }
                    }
                    Thread.sleep(3000);
                    if (Thread.interrupted()) break;
                } catch (Exception e) {
                    logger.error("checking vip error",e);
                    break;
                }
            }
        }
    }

}
