package cn.mageek.datanode.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Iterator;
import java.util.Map;
import static cn.mageek.datanode.main.DataNode.DATA_EXPIRE;
import static cn.mageek.datanode.main.DataNode.DATA_POOL;

/**
 * 检查过期键
 * @author Mageek Chiu
 * @date 2018/5/19 0007:16:36
 */
public class ExpireChecking extends DataRunnable{

    private static final Logger logger = LoggerFactory.getLogger(ExpireChecking.class);

    @Override
    public void run(){

        Long noTime = System.currentTimeMillis();
        int i = 0;

        Iterator<Map.Entry<String,Long>> it = DATA_EXPIRE.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry<String,Long> e = it.next();
            String k = e.getKey();
            Long v = e.getValue();
            if (v<noTime){// 已经过期，删除两个键与值
                it.remove();
                DATA_POOL.remove(k);
                i++;
            }
        }
        logger.debug("deleted {} keys",i);
    }
}
