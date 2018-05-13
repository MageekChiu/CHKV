package cn.mageek.common.res;

/**
 * @author Mageek Chiu
 * @date 2018/5/9 0009:9:26
 */
public class Constants {
    public static final String innerSplit = "\r\n";// 一个命令内部行之间间隔
    public static final String outerSplit = "\t\n";// 不同命令间隔

    public static final double pageSize = 5;// 一次转移几条数据

    public static final String offlineKey = "*#*OFFLINE*#*";
    public static final String offlineValue = "true";
    public static final String onlineValue = "false";

    public static final String IDSplitter = "__@__";
}
