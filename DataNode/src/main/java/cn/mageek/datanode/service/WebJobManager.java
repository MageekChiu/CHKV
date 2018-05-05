package cn.mageek.datanode.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

/**
 *
 * @author Mageek Chiu
 * @date 2018/5/4 0007:16:24
 */
public class WebJobManager implements Runnable{


    private static final Logger logger = LoggerFactory.getLogger(WebJobManager.class);

    private CountDownLatch countDownLatch;

    public WebJobManager(CountDownLatch countDownLatch) {
        this.countDownLatch = countDownLatch;
    }

    public void run() {

    }


}

