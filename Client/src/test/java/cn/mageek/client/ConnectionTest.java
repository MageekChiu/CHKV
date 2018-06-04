package cn.mageek.client;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Mageek Chiu
 * @date 2018/5/10 0010:20:51
 */
public class ConnectionTest {

    private static final Logger logger = LoggerFactory.getLogger(ConnectionTest.class);


    public static void main(String... arg) throws Exception {
//        Connection connection = new Connection();
//        connection.connect();

        Client client = new Client();
        client.connect("192.168.0.136","10102");

//        logger.debug(client.set("192.168.0.136:10099","123456")+"");
//        logger.debug(client.get("192.168.0.136:10099")+"");
        logger.debug(client.set("112","23")+"");
//        logger.debug(client.set("nba","23")+"");
//        logger.debug(client.set("nba rock","23")+"");
//        logger.debug(client.del("1321")+"");
//        logger.debug(client.keys("nba").toString());
//        logger.debug(client.keys("*").toString());
//        logger.debug(client.del("112")+"");
//        logger.debug(client.expire("nba",5)+"");// 5秒过期
//        logger.debug(client.get("nba")+"");
//        Thread.sleep(6000);
//        logger.debug(client.get("nba")+"");

//        logger.debug(client.incr("nba")+"");
//        logger.debug(client.incr("nba")+"");
        logger.debug(client.append("112","a")+"");
        logger.debug(client.append("112","a")+"");

//
        new Thread(() -> {
//            logger.debug(client.incr("nba")+"");
//            logger.debug(client.incr("nba")+"");
//            logger.debug(client.decr("nba")+"");
//            logger.debug(client.incr("nba")+"");
//            logger.debug(client.incr("nba")+"");
            logger.debug(client.append("112","b")+"");
            logger.debug(client.append("112","b")+"");
            logger.debug(client.append("112","b")+"");
            logger.debug(client.append("112","b")+"");
//            logger.debug(client.incr("nba")+"");
        }).start();
//
//        logger.debug(client.incr("nba")+"");
//        logger.debug(client.decr("nba")+"");
//        logger.debug(client.decr("nba")+"");

        logger.debug(client.append("112","a")+"");
        logger.debug(client.append("112","a")+"");



//        client.close();



//        try(Client client = new Client("192.168.0.136","10102")){
//            logger.debug(client.set("192.168.0.136:10099","123456")+"");
//            logger.debug(client.get("192.168.0.136:10099")+"");
//            logger.debug(client.set("112","23")+"");
//            logger.debug(client.del("1321")+"");
//            logger.debug(client.del("112")+"");
//        }

    }

    @Test
    public void SplitTest(){
        // 结果都是2 所以末尾有没有分隔符都一样
        String a = "aa\r\nbb";
        logger.debug("{}",a.split("\r\n").length);//2
        a = "aa\r\nbb\r\n";
        logger.debug("{}",a.split("\r\n").length);//2


        a = "aa";
        logger.debug("{}",a.split("\r\n").length);//1

    }

    @Test
    public void regexTest(){
        String regex = "a";
        Matcher matcher = Pattern.compile(regex).matcher("sssa");
        logger.debug(String.valueOf(matcher.matches()));

    }
}