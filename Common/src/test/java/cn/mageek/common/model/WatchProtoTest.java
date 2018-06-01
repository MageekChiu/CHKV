package cn.mageek.common.model;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.junit.Assert.*;

/**
 * @author Mageek Chiu
 * @date 2018/6/1 0001:13:14
 */
public class WatchProtoTest {
    private static final Logger logger = LoggerFactory.getLogger(WatchProtoTest.class);

    @Test
    public void protoTest(){
        WatchReqProto.WatchReq.Builder builder = WatchReqProto.WatchReq.newBuilder();
        builder.setImmediately(false);
        WatchReqProto.WatchReq watchReq = builder.build();
        logger.info("req:{}",watchReq.getImmediately());

        WatchRespProto.WatchResp.Builder builder1 = WatchRespProto.WatchResp.newBuilder();
        builder1.putHashCircle(1,"asdasd");
        WatchRespProto.WatchResp watchResp = builder1.build();
        logger.info("resp:{}",watchResp.getHashCircleMap());
    }

}