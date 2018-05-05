package cn.mageek.common.model;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;

/**
 * 服务端<------>客户端的消息对象
 * @author Mageek Chiu
 * @date 2018/3/7 0007:18:56
 */
public class RcvMsgObject {
    private final String header;
    private final String mac;
    private final String command;
    private final String para;
    private final int timestamp;
    private final short dataLength;
    private final ByteBuf data;
    private final String status;
    private final String footer ;

    public RcvMsgObject(String header, String mac, String status, String command, String para, short dataLength, int timestamp, ByteBuf data, String footer) {
        this.header = header;
        this.mac = mac;
        this.status = status;
        this.command = command;
        this.para = para;
        this.dataLength =  dataLength;
        this.timestamp = timestamp;
        this.data = data;
        this.footer = footer;
    }

    public String getFooter() {
        return footer;
    }

    public String getMac() {
        return mac;
    }

    public String getHeader() {
        return header;
    }

    public String getStatus() {
        return status;
    }

    public String getCommand() {
        return command;
    }

    public short getDataLength() {
        return dataLength;
    }

    public ByteBuf getData() {
        return data;
    }

    public String getPara() {
        return para;
    }

    public int getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "\theader:" + header + "\n" +
                "\tmac" + mac + "\n" +
                "\tcommand:" + command + "\n" +
                "\tpara:" + para + "\n" +
                "\ttimestamp:" + timestamp + "\n" +
                "\tdataLength:" + dataLength + "\n" +
                "\tdata:" + ByteBufUtil.hexDump(data) + "\n" +
                "\tstatus:" + status + "\n" +
                "\tfooter:" + footer + "\n" ;
    }
}
