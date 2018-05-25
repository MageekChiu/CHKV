package cn.mageek.datanode.job;

import cn.mageek.datanode.res.JobFactory;
import cn.mageek.datanode.service.CronJobManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static cn.mageek.datanode.service.CronJobManager.isMaster;
import static cn.mageek.datanode.service.CronJobManager.useHA;

/**
 * 主从同步，用于实现DataNode的主从复制，高可用
 * @author Mageek Chiu
 * @date 2018/5/25 0025:10:40
 */
public class MSSync extends DataRunnable{
    private static final Logger logger = LoggerFactory.getLogger(MSSync.class);


    private String IPPort;
//    private ExecutorService service = Executors.newFixedThreadPool(1);

    @Override
    public void run() {
        if (useHA && isMaster) {// 使用HA且是master才同步到slave节点，暂时采用全量复制
            DataTransfer dataTransfer = (DataTransfer) JobFactory.getJob("DataTransfer");
            dataTransfer.connect(IPPort, true, true);
            try {
                dataTransfer.run();// 直接在本线程运行
            }catch (Exception e){
                logger.error("sync error:{}",e.getMessage());
            }

            //            service.execute(dataTransfer);// 用线程池运行
        }
    }

    public void connect(String IPPort) {
        this.IPPort = IPPort;
    }
}
