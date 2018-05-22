package cn.mageek.client;

import cn.mageek.common.ha.HAThirdParty;
import cn.mageek.common.ha.ZKThirdParty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

import static cn.mageek.client.ZKServerTest.*;

/**
 * @author Mageek Chiu
 * @date 2018/5/21 0010:18:51
 */
public class ZKClientTest {

    private static final Logger logger = LoggerFactory.getLogger(ZKClientTest.class);

    public static void main(String... arg) {
        HAThirdParty party = new ZKThirdParty(CONNECT_ADDR,SESSION_TIMEOUT,CONNECTION_TIMEOUT,MASTER_NODE_PATH,1000,10);
        party.getInstantMaster();

        Consumer<String> consumer = s -> {
            if (s==null){
                logger.error("masterNode is down, waiting");
            }else{
                logger.info("masterNode may have changed:{}",s);
            }
        };

        party.beginWatch(consumer);
        while (party.getMasterNode()==null);//忙等待
        logger.debug(party.getMasterNode());

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