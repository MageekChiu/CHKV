package cn.mageek.client;

import cn.mageek.common.ha.HAThirdParty;
import cn.mageek.common.ha.ZKThirdParty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.util.function.Consumer;

/**
 * @author Mageek Chiu
 * @date 2018/5/21 0010:18:51
 */
public class ZKServerTest {

    private static final Logger logger = LoggerFactory.getLogger(ZKServerTest.class);

    public static final String CONNECT_ADDR = "127.0.0.1:2181,127.0.0.1:3181,127.0.0.1:4181";
    public static final int SESSION_TIMEOUT = 2000;
    public static final int CONNECTION_TIMEOUT = 8000;
    public static final String MASTER_NODE_PATH = "/CHKV/masterNode";
    public static String thisNode;
    public static String masterNode;

    public static void main(String... arg) {
        thisNode = ManagementFactory.getRuntimeMXBean().getName();
        logger.debug("thisNode: {}",thisNode);// 如果是NameNode就存储 面向dataNode的ip:port:面向client的ip:port；

        HAThirdParty party = new ZKThirdParty(CONNECT_ADDR,SESSION_TIMEOUT,CONNECTION_TIMEOUT,MASTER_NODE_PATH,1000,10);
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
                logger.error("masterNode is down, try to become Master");
                if (party.becomeMaster()){
                    logger.info("Successfully tried to Became Master");
                }else {
                    logger.info("Failed to try to Became Master");
                }
            }else{
                logger.info("masterNode:{}",s);
            }
        };
        party.beginWatch(consumer);

        while (true){//防止线程结束
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
    }

}