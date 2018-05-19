package cn.mageek.datanode.res;

import cn.mageek.datanode.job.DataRunnable;
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
public class JobFactory {
    private static String packagePrefix = "cn.mageek.datanode.job.";
    private static final Logger logger = LoggerFactory.getLogger(JobFactory.class);
    private static volatile Map<String,DataRunnable> jobMap;
//    private static volatile Map<String,Runnable> jobMap;
//    private static volatile Map<String,String> DATA_POOL ;// 数据存储池


//    public static void construct(Map<String,String> dataPool) throws Exception {
    public static void construct() throws Exception {

        if(jobMap ==null){//volatile+双重检查来实现单例模式
            synchronized (JobFactory.class){
                if (jobMap == null){
//                    DATA_POOL = dataPool;
                    getAllJobs();
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


    private static void getAllJobs() throws Exception {
        jobMap = new ConcurrentHashMap<>();

        Reflections reflections = new Reflections(packagePrefix);

        Set<Class<? extends DataRunnable>> subTypes = reflections.getSubTypesOf(DataRunnable.class);
//        Set<Class<? extends Runnable>> subTypes = reflections.getSubTypesOf(Runnable.class);
        int idStart = packagePrefix.length();
        for(Class clazz : subTypes){
            String className = clazz.getName();
            String jobName = className.substring(idStart);
            logger.debug("Job class found: {} , jobName: {}",className,jobName);
            DataRunnable r = (DataRunnable) clazz.newInstance();
            r.connect();//
//            Runnable r = (Runnable) clazz.newInstance();
            jobMap.put(jobName,r);
        }
    }

}
