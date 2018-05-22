package cn.mageek.client;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static cn.mageek.client.ZooKeeperServerTest.*;

/**
 * @author Mageek Chiu
 * @date 2018/5/10 0010:20:51
 */
public class ZooKeeperClientTest {

    private static final Logger logger = LoggerFactory.getLogger(ZooKeeperClientTest.class);

    private static volatile String masterInfo = null;

    public static void main(String... arg) throws Exception {
        // 构造连接
        CuratorFramework curator = CuratorFrameworkFactory
                .builder()
                .connectString(CONNECT_ADDR)
                .connectionTimeoutMs(CONNECTION_TIMEOUT)//连接创建超时时间
                .sessionTimeoutMs(SESSION_TIMEOUT)//会话超时时间
                .retryPolicy(policy)
                .build();
        curator.start();

        // 监听
        NodeCache cache = new NodeCache(curator, MASTER_NODE_PATH,false);
        cache.getListenable().addListener(()->{
            ChildData data = cache.getCurrentData();
            if (data != null){
                String path = data.getPath();
                Stat stat = data.getStat();
                String dataString = new String(data.getData());
                logger.debug("masterNode info, path:{},data:{},stat,{}",path,dataString,stat);
                masterInfo = dataString;
            }else {
                logger.info("masterNode is down, waiting");
            }
        });
        cache.start(true);


        // 获得主机，阻塞等待
        try {
            masterInfo =  new String(curator.getData().forPath(MASTER_NODE_PATH));
        }catch (Exception e){
            logger.error("no masterInfo");
            masterInfo = null;
        }
        while (masterInfo==null);
        logger.info("masterInfo:{}",masterInfo);

        while (true){//防止线程结束
            Thread.sleep(2000);
        }

    }

}