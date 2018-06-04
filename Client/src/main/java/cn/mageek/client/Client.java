package cn.mageek.client;

import cn.mageek.common.model.DataRequest;
import cn.mageek.common.model.DataResponse;

import java.util.List;

import static cn.mageek.common.model.LineType.SINGLE_ERROR;

/**
 * @author Mageek Chiu
 * @date 2018/5/10 0010:19:17
 */
public class Client extends Connection {

    public Client() {

    }

    public Client(String nameNodeIP,String nameNodePort) {
        super(nameNodeIP,nameNodePort);
    }

    /**
     *
     * @param key key
     * @param value value
     * @return 设置是否成功
     */
    public boolean set(String key, String value){
        DataRequest request = new DataRequest("SET",key,value);
        DataResponse r =  sendCommand(request);// 自动生成ID
        return !r.getLineType().equals(SINGLE_ERROR);
    }

    /**
     *
     * @param key key
     * @return 删除键个数
     */
    public int del(String key){
        DataRequest request = new DataRequest("DEL",key,"");
        DataResponse r =  sendCommand(request);
        return r.getLineType().equals(SINGLE_ERROR) ? -1 : Integer.parseInt(r.getMsg());
    }

    public String get(String key){
        DataRequest request = new DataRequest("GET",key,"");
        DataResponse r =  sendCommand(request);
        return r.getMsg();
    }

    public List<String> keys(String key){
        DataRequest request = new DataRequest("KEYS",key,"");
        DataResponse r =  sendCommand(request);
        return r.getMsgList();
    }

    public int expire(String key,long value){
        DataRequest request = new DataRequest("EXPIRE",key,String.valueOf(value));
        DataResponse r =  sendCommand(request);
        return r.getLineType().equals(SINGLE_ERROR) ? -1 : Integer.parseInt(r.getMsg());
    }

    public int incr(String key){
        DataRequest request = new DataRequest("INCR",key,"");
        DataResponse r =  sendCommand(request);
        return r.getLineType().equals(SINGLE_ERROR) ? -1 : Integer.parseInt(r.getMsg());
    }
    public int decr(String key){
        DataRequest request = new DataRequest("DECR",key,"");
        DataResponse r =  sendCommand(request);
        return r.getLineType().equals(SINGLE_ERROR) ? -1 : Integer.parseInt(r.getMsg());
    }

    public String append(String key,String value){
        DataRequest request = new DataRequest("APPEND",key,value);
        DataResponse r =  sendCommand(request);
        return r.getMsg();
    }

}
