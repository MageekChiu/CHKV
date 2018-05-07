package cn.mageek.common.model;

/**
 * 客户端请求数据
 * @author Mageek Chiu
 * @date 2018/5/6 0006:13:28
 */
public class DataRequest {

    private String command;
    private String key;
    private String value;
    private String dataType = DataType.A_STRING;

    public DataRequest(String command, String key, String value) {
        this.command = command;
        this.key = key;
        this.value = value;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    @Override
    public String toString() {
        return "DataRequest -- command: "+command+", key: "+key+", value: "+value+", dataType: "+dataType;
    }
}
