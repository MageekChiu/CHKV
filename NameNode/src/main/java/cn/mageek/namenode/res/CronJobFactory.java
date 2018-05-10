package cn.mageek.namenode.res;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AbstractDataNodeCommand 工厂类
 * @author Mageek Chiu
 * @date 2018/3/13 0013:21:49
 */
public class CronJobFactory {
    private static String packagePrefix = "cn.mageek.datanode.cron.";
    private static final Logger logger = LoggerFactory.getLogger(CronJobFactory.class);
    private static volatile Map<String,Runnable> cronJobMap;

    public static void construct() throws Exception {
        if(cronJobMap ==null){//volatile+双重检查来实现单例模式
            synchronized (CronJobFactory.class){
                if (cronJobMap ==null){
                    cronJobMap = new ConcurrentHashMap<>();
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
            cronJobMap.put(jobName,(Runnable)clazz.newInstance());
        }
    }

}
