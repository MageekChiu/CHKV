package cn.mageek.common.ha;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;


/**
 * @author Mageek Chiu
 * @date 2018/5/21 0021:12:35
 */
public class ZKThirdParty extends HAThirdParty {

    private static final Logger logger = LoggerFactory.getLogger(ZKThirdParty.class);

    private CuratorFramework curator ;

    private String connectAddr;
    private int sessionTimeout;
    private int connectionTimeout;
    private String masterNodePath;
    private RetryPolicy policy;

    public ZKThirdParty(String connectAddr, int sessionTimeout, int connectionTimeout, String masterNodePath,int baseSleepTimeMs,int maxRetries) {
        this.connectAddr = connectAddr;
        this.sessionTimeout = sessionTimeout;
        this.connectionTimeout = connectionTimeout;
        this.masterNodePath = masterNodePath;
        this.policy = new ExponentialBackoffRetry(baseSleepTimeMs,maxRetries);
        getCon();
    }

    @Override
    public void getCon() {
        CuratorFramework curator = CuratorFrameworkFactory
                .builder()
                .connectString(connectAddr)
                .connectionTimeoutMs(connectionTimeout)//连接创建超时时间
                .sessionTimeoutMs(sessionTimeout)//会话超时时间
                .retryPolicy(policy)
                .build();
        curator.start();
        this.curator = curator;
    }

    @Override
    public void releaseCon() {
        connectAddr = null;
        masterNode = null;
        policy = null;
        curator.close();
    }

    @Override
    public boolean becomeMaster() {
        String path= "";
        try {
            path =  curator.create()
                    .creatingParentContainersIfNeeded()
                    .withMode(CreateMode.EPHEMERAL)
                    .forPath(masterNodePath,thisNode.getBytes());// 存的值应该是本服务器的信息以及后面所有应用服务器的信息
            logger.debug(path);
        }catch (Exception e){
            logger.error("becomeMaster Exception: {}",e.getMessage());
        }
        return masterNodePath.equals(path);
    }

    @Override
    public String getInstantMaster() {
        try {
            setMasterNode(new String(curator.getData().forPath(masterNodePath)));
        }catch (Exception e){
            logger.error("getInstantMaster Exception:{} ",e.getMessage());
            setMasterNode(null);
        }
        return getMasterNode();
    }

    @Override
    public void beginWatch(Consumer<String> nameNodeChanged) {
        NodeCache cache = new NodeCache(curator, masterNodePath,false);
        cache.getListenable().addListener(()->{
            ChildData data = cache.getCurrentData();
            if (data != null){
//                String path = data.getPath();
//                Stat stat = data.getStat();
                String dataString = new String(data.getData());
//                logger.debug("masterNode info, path:{},data:{},stat,{}",path,dataString,stat);
                setMasterNode(dataString);
            }else {
//                logger.info("masterNode is down, waiting");
                setMasterNode(null);
            }
            nameNodeChanged.accept(getMasterNode());
        });
        try {
            cache.start(true);
        } catch (Exception e) {
            logger.error("beginWatch Exception",e);
        }
    }
}
