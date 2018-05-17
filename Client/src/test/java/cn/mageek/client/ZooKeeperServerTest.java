package cn.mageek.client;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.lang.management.ManagementFactory;

/**
 * @author Mageek Chiu
 * @date 2018/5/10 0010:20:51
 */
public class ZooKeeperServerTest {

    private static final Logger logger = LoggerFactory.getLogger(ZooKeeperServerTest.class);

    public static final String CONNECT_ADDR = "127.0.0.1:2181,127.0.0.1:3181,127.0.0.1:4181";
    public static final int SESSION_TIMEOUT = 2000;
    public static final int CONNECTION_TIMEOUT = 8000;
    public static final String MASTER_NODE_PATH = "/example/masterNode";
    public static final RetryPolicy policy = new ExponentialBackoffRetry(1000,10);
    public static String thisNode;
    public static String masterNode;

    public static void main(String... arg) throws Exception {

        thisNode = ManagementFactory.getRuntimeMXBean().getName();
        logger.debug("my pid: {}",thisNode);

        // 构造连接
        CuratorFramework curator = CuratorFrameworkFactory
                .builder()
                .connectString(CONNECT_ADDR)
                .connectionTimeoutMs(CONNECTION_TIMEOUT)//连接创建超时时间
                .sessionTimeoutMs(SESSION_TIMEOUT)//会话超时时间
                .retryPolicy(policy)
                .build();
        curator.start();

        // 创建节点也就是成为master，阻塞等待
        boolean result = becomeMaster(curator);
        if (result){
            logger.info("Successfully Became Master");
        }else {
            logger.info("Failed to Became Master");
        }

        // 获取结果并再次确认
        boolean result1 =  confirm(curator);
        if (result1){
            logger.info("Confirmed, I am the Master,masterNode;{}",masterNode);
        }else {
            logger.info("Confirmed,I am the Standby,masterNode;{}",masterNode);
        }

        // 监听
        NodeCache cache = new NodeCache(curator, MASTER_NODE_PATH,false);
        cache.getListenable().addListener(()->{
            ChildData data = cache.getCurrentData();
            if (data != null){
                String path = data.getPath();
                Stat stat = data.getStat();
                String dataString = new String(data.getData());
                logger.debug("masterNode info, path:{},data:{},stat,{}",path,dataString,stat);
            }else {
                logger.info("masterNode is down, try to become Master");
                if (becomeMaster(curator)){
                    logger.info("Successfully tried to Became Master");
                }else {
                    logger.info("Failed to try to Became Master");

                }
            }
        });
        cache.start(true);

        while (true){//防止线程结束
            Thread.sleep(2000);
        }


    }

    // 确认master
    private static boolean confirm(CuratorFramework curator) throws Exception {
        masterNode = new String(curator.getData().forPath(MASTER_NODE_PATH));
        logger.info("masterNode: {}",masterNode);
//        data = new String(curator.getData().forPath("/none"));// 没有该key的话就直接报exception
        return thisNode.equals(masterNode);

    }

    // 成为master
    private static boolean becomeMaster(CuratorFramework curator) throws Exception {
//        AtomicBoolean result = new AtomicBoolean(false);
////
////        // 异步创建节点，不会报异常
////        Executor executor = Executors.newFixedThreadPool(1);
////        curator.create()
////                .creatingParentContainersIfNeeded()
////                .withMode(CreateMode.EPHEMERAL)
////                .inBackground((curatorFramework,event)->{
////                    // 成功getResultCode 0，失败 -110
////                    logger.debug("getType: {}, getResultCode: {}, getName: {}, getData: {}",event.getType(),event.getResultCode(),event.getName(),event.getData());
////                    if (event.getResultCode()==0) result.set(true);
////                },executor)
////                .forPath(MASTER_NODE_PATH,thisNode.getBytes());
////
////        // 等待结果
////        Thread.sleep(5000);
////        return result.get();

        // 同步创建节点，已存在会报异常
        String path= "";
        try {
             path =  curator.create()
                    .creatingParentContainersIfNeeded()
                    .withMode(CreateMode.EPHEMERAL)
                    .forPath(MASTER_NODE_PATH,thisNode.getBytes());// 存的值应该是本负载服务器的信息以及后面所有应用服务器的信息
            logger.debug(path);
        }catch (Exception e){
            logger.error(e.getMessage());
        }
        return MASTER_NODE_PATH.equals(path);
    }

}