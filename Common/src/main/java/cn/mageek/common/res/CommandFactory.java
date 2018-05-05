package cn.mageek.common.res;

import cn.mageek.common.command.Command;
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
public class CommandFactory {
    private static final String packageDir = "cn/mageek/NetServer/command";
    private static final String packagePrefix = "cn.mageek.common.command.";
    private static final Logger logger = LoggerFactory.getLogger(CommandFactory.class);

    private static volatile Map<String,Command> commandMap ;

    public static void construct() throws Exception {
        if(commandMap==null){//volatile+双重检查来实现单例模式
            synchronized (CommandFactory.class){
                if (commandMap==null){
                    commandMap = new ConcurrentHashMap<>();
                    // Command 池 如果初始化不成功 整个程序就无法正常运转，所以不用try catch, 直接采用快速失败原则
                    getAllCommands(commandMap);
                    logger.info("Command pool initialized, number : {}",commandMap.size());
                }
            }
        }
    }

    public static Command getCommand(String commandId){
        return commandMap.get(commandId);
    }

    public static void destruct(){
        commandMap = null;
    }


    private static void getAllCommands(Map<String,Command> commandMap) throws Exception {

        Reflections reflections = new Reflections(packagePrefix);

        Set<Class<? extends Command>> subTypes = reflections.getSubTypesOf(Command.class);

        int idStart = packagePrefix.length()+7;
        for(Class clazz : subTypes){
            String className = clazz.getName();
            String commandId = className.substring(idStart);
            logger.debug("Command class found: {} , Id: {}",clazz.getName(),commandId);
            commandMap.put(commandId,(Command)clazz.newInstance());
        }
    }

}
