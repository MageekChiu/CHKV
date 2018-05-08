package cn.mageek.datanode.res;

import cn.mageek.datanode.cron.Heartbeat;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Command 工厂类
 * @author Mageek Chiu
 * @date 2018/3/13 0013:21:49
 */
public class CronJobFactory {
    private static String packagePrefix = "cn.mageek.datanode.cron.";
    private static final Logger logger = LoggerFactory.getLogger(CronJobFactory.class);
    private static volatile Map<String,Runnable> cronJobMap;
    private static volatile Map<String,String> DATA_POOL ;// 数据存储池


    public static void construct(Map<String,String> dataPool) throws Exception {
        if(cronJobMap ==null){//volatile+双重检查来实现单例模式
            synchronized (CronJobFactory.class){
                if (cronJobMap == null){
                    cronJobMap = new ConcurrentHashMap<>();
                    DATA_POOL = dataPool;
                    getAllCronJobs(cronJobMap);
                    logger.info("CronJob pool initialized, number : {}", cronJobMap.size());
                }
            }
        }
    }

    public static Runnable getCronJob(String jobName){
        return cronJobMap.get(jobName);
    }

    public static void destruct(){
        cronJobMap = null;
    }


    private static void getAllCronJobs( Map<String,Runnable> cronJobMap) throws Exception {

        Reflections reflections = new Reflections(packagePrefix);

        Set<Class<? extends Runnable>> subTypes = reflections.getSubTypesOf(Runnable.class);
        int idStart = packagePrefix.length();
        for(Class clazz : subTypes){
            String className = clazz.getName();
            String jobName = className.substring(idStart);
            logger.debug("CronJob class found: {} , jobName: {}",className,jobName);
            Runnable r = (Runnable) clazz.newInstance();
            if (r instanceof Heartbeat){
                ((Heartbeat) r).connect(DATA_POOL);// 这个是定时任务的特例，心跳需要提前建立连接
            }
            cronJobMap.put(jobName,r);
        }
    }

}
