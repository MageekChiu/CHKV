package cn.mageek.datanode.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

import static cn.mageek.datanode.service.DataManager.clientMap;

/**
 * 管理客户端的在线状态的handler
 * @author Mageek Chiu
 * @date 2018/3/5 0005:19:02
 */
//@ChannelHandler.Sharable//必须是线程安全的
public class ClientHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);
    private static final AtomicInteger clientNumber = new AtomicInteger(0);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        String connection = ctx.channel().remoteAddress().toString();
        clientMap.put(connection,ctx.channel());
        logger.info("new connection arrived: {} clients living {}",connection, clientMap.size());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String connection = ctx.channel().remoteAddress().toString();
        clientMap.remove(connection);
        logger.info("connection closed: {},uuid:{}, clients living {}",connection, clientMap.size());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ctx.fireChannelRead(msg);//传输到下一个inBound
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("receiveMsg from: {}，error: ",ctx.channel().remoteAddress(),cause);//ReadTimeoutException 会出现在这里，亦即事件会传递到handler链中最后一个事件处理中
        ctx.close();//这时一般就会自动关闭连接了。手动关闭的目的是避免偶尔情况下会处于未知状态
    }
}
