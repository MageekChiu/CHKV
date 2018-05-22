package cn.mageek.common.util;

import static cn.mageek.common.res.Constants.IPSplitter;

/**
 * @author Mageek Chiu
 * @date 2018/5/22 0022:15:35
 */
public class HAHelper {

    private String[] strings;

    public static String getNodeString(String dataNodeIP,String dataNodePort,String clientIP,String clientPort){
        return dataNodeIP+IPSplitter+dataNodePort+IPSplitter+clientIP+IPSplitter+clientPort;
    }

    public HAHelper(String s) {
        strings = s.split(IPSplitter);
    }

    public String getDataNodeIP(){
        return strings[0];
    }

    public String getDataNodePort(){
        return strings[1];
    }

    public String getClientIP(){
        return strings[2];
    }

    public String getClientPort(){
        return strings[3];
    }
}
