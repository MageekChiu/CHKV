package cn.mageek.common.cron;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static cn.mageek.common.res.ConstPool.L_SUCCESS;

/**
 * @author Mageek Chiu
 * @date 2018/3/7 0007:19:44
 */
public class OnlineCount implements Runnable{
    private static final Logger logger = LoggerFactory.getLogger(OnlineCount.class);

    public void run(){
        logger.info(L_SUCCESS);
    }
}
