package cn.mageek.client.job;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Mageek Chiu
 * @date 2018/5/10 0010:20:51
 */
public class ConnectionTest {

    public static void main(String... arg) {
        Connection connection = new Connection();
        connection.connect("192.168.0.136","10102");
    }
}