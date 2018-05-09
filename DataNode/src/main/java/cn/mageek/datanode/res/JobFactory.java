package cn.mageek.datanode.res;

import cn.mageek.datanode.jobs.DataRunnable;
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
public class JobFactory {
    private static String packagePrefix = "cn.mageek.datanode.jobs.";
    private static final Logger logger = LoggerFactory.getLogger(JobFactory.class);
    private static volatile Map<String,Runnable> jobMap;
    private static volatile Map<String,String> DATA_POOL ;// 数据存储池


    public static void construct(Map<String,String> dataPool) throws Exception {
        if(jobMap ==null){//volatile+双重检查来实现单例模式
            synchronized (JobFactory.class){
                if (jobMap == null){
                    jobMap = new ConcurrentHashMap<>();
                    DATA_POOL = dataPool;
                    getAllJobs(jobMap);
                    logger.info("Job pool initialized, number : {}", jobMap.size());
                }
            }
        }
    }

    public static Runnable getJob(String jobName){
        return jobMap.get(jobName);
    }

    public static void destruct(){
        jobMap = null;
    }


    private static void getAllJobs(Map<String,Runnable> jobMap) throws Exception {

        Reflections reflections = new Reflections(packagePrefix);

        Set<Class<? extends Runnable>> subTypes = reflections.getSubTypesOf(Runnable.class);
        int idStart = packagePrefix.length();
        for(Class clazz : subTypes){
            String className = clazz.getName();
            String jobName = className.substring(idStart);
            logger.debug("Job class found: {} , jobName: {}",className,jobName);
            DataRunnable r = (DataRunnable) clazz.newInstance();
            r.connect(DATA_POOL);// 每个job都要传入数据存储池，备用
            jobMap.put(jobName,r);
        }
    }

}
