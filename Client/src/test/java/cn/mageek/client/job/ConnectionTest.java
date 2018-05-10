package cn.mageek.client.job;

import cn.mageek.client.Client;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Mageek Chiu
 * @date 2018/5/10 0010:20:51
 */
public class ConnectionTest {

    public static void main(String... arg) throws InterruptedException {
//        Connection connection = new Connection();
//        connection.connect();

        Client client = new Client();
        client.connect("192.168.0.136","10102");
        client.set("asd","sd");
        Thread.sleep(3000);
//        client.set("asd3223213fg","asdfg");
//        client.set("1234AA56","123456");
        client.set("192.168.0.136:10099","123456");
        client.get("192.168.0.136:10099");
        client.get("112");

    }
}