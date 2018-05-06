package cn.mageek.common.model;

/**
 * @author Mageek Chiu
 * @date 2018/5/6 0006:13:32
 */
public class LineType {

    public static final String SINGLE_RIGHT = "+";  //表示一个正确的状态信息，具体信息是当前行+后面的字符。
    public static final String SINGLE_ERROR = "-";  //表示一个错误信息，具体信息是当前行－后面的字符。
    public static final String LINE_NUM = "*";      //表示消息体总共有多少行，不包括当前行,*后面是具体的行数。
    public static final String NEXT_LEN = "$";      //表示下一行数据长度，不包括换行符长度\r\n,$后面则是对应的长度的数据。
    public static final String INT_NUM = ":";       //表示返回一个数值，：后面是相应的数字节符。

}
