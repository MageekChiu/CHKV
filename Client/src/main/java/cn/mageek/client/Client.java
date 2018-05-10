package cn.mageek.client;

import cn.mageek.client.job.Connection;
import cn.mageek.common.model.DataRequest;

/**
 * @author Mageek Chiu
 * @date 2018/5/10 0010:19:17
 */
public class Client extends Connection {

    public Client() {

    }

    public void set(String key, String value){
        DataRequest request = new DataRequest("SET",key,value);
        sendCommand(request);
    }

    public void del(String key){
        DataRequest request = new DataRequest("DEL",key,"");
        sendCommand(request);
    }

    public String get(String key){
        DataRequest request = new DataRequest("GET",key,"");
        sendCommand(request);
        return "";
    }

}
