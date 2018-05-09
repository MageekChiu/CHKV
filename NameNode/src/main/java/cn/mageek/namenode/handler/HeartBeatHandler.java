package cn.mageek.namenode.handler;

import cn.mageek.common.model.HeartbeatRequest;
import cn.mageek.common.model.HeartbeatResponse;
import cn.mageek.common.util.ConsistHash;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicReference;

import static cn.mageek.common.model.HeartbeatType.*;

/**
 * @author Mageek Chiu
 * @date 2018/5/7 0007:13:52
 */
public class HeartBeatHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(HeartBeatHandler.class);
    //key表示dataNode的/ip:port，value表示对应信道 ， 上线就有
    private Map<String,Channel> dataNodeMap;
    //key表示dataNode的/ip:port，value表示其对客户端开放的ip:port  ， 发送心跳就有
    private Map<String,String> dataNodeClientMap ;
    //key表示dataNode的hash值，value表示其对客户端开放的ip:port  ， 发送心跳状态更新后就有
    private ConcurrentSkipListMap<Integer, String> sortedServerMap ;

    public HeartBeatHandler(Map<String,Channel> dataNodeMap,Map<String,String> dataNodeClientMap ,ConcurrentSkipListMap<Integer, String> sortedServerMap){
        this.dataNodeMap = dataNodeMap;
        this.dataNodeClientMap = dataNodeClientMap;
        this.sortedServerMap = sortedServerMap;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        String connection = ctx.channel().remoteAddress().toString();
        // 维护集合
        dataNodeMap.put(connection,ctx.channel());
        logger.info("new connection arrived: {},, clients living {}",connection, dataNodeMap.size());//包含ip:port
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String connection = ctx.channel().remoteAddress().toString();
        // 维护集合
        dataNodeMap.remove(connection);
        String IPPort = dataNodeClientMap.get(connection);
        sortedServerMap.forEach((k,v)->{
            if (v.equals(IPPort)) sortedServerMap.remove(k);
        });
        dataNodeClientMap.remove(connection);
        logger.info("connection closed: {},, clients living {}",connection,dataNodeMap.size());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String connection = ctx.channel().remoteAddress().toString();
        HeartbeatRequest request = (HeartbeatRequest)msg ;
        logger.debug("NameNode received: {}" , request);
        // 维护集合
        dataNodeClientMap.put(connection,request.getIPPort());

        HeartbeatResponse response = handleStatus(connection,request);
        logger.debug("NameNode answered: {}" , response);
        ctx.writeAndFlush(response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("receiveMsg from: {}，error: ",ctx.channel().remoteAddress(),cause);
        ctx.close();
    }

    private HeartbeatResponse handleStatus(String connection,HeartbeatRequest request){
        HeartbeatResponse response;
        String IPPort,nextIPPort;
        ConsistHash hash = new ConsistHash(sortedServerMap,dataNodeClientMap);// 此时dataNodeClientMap是最新的，据此更新sortedServerMap
        switch (request.getStatus()){
            case ONLINE : // 上线，要通知环中下一台dataNode分一部分数据给本个dataNode
                IPPort = dataNodeClientMap.get(connection);
                nextIPPort = hash.getServer(IPPort,true);
//                 根据IPPort找到connection，在找到channel，然后就可以 发消息了
                 AtomicReference<String> nextConnection = new AtomicReference<>("");
                 dataNodeClientMap.forEach((k,v)->{
                     if (v.equals(nextIPPort)) nextConnection.set(k);
                 });
                logger.debug("{} ONLINE, nextIPPort {}, nextConnection {}",IPPort,nextIPPort,nextConnection.get());
                if (IPPort.equals(nextIPPort)){// 是第一台上线
                    logger.info("only 1 dataNode {}, Skip the procedure",IPPort);
                    response = new HeartbeatResponse(true, dataNodeClientMap.size(),null);//connection依然运行，不需要转移数据
                    break;
                }
                Channel nextChannel = dataNodeMap.get(nextConnection.get());
                 if (nextChannel != null){
                     HeartbeatResponse response1 = new HeartbeatResponse(true, dataNodeClientMap.size(),dataNodeClientMap.get(connection));// nextConnection依然运行，只是分一部分数据给connection
                     logger.debug("NameNode pushed : {} to {}" ,response1,connection);
                     nextChannel.writeAndFlush(response1);
                 }
                 response = new HeartbeatResponse(true, dataNodeClientMap.size(),null);// connection依然运行，不需要转移数据
                 break;
            case OFFLINE : // 下线，要通知本个dataNode把数据全部转移至环中下一台dataNode
                 IPPort = dataNodeClientMap.get(connection);
                 nextIPPort = hash.getServer(IPPort,true);
                logger.debug("{} OFFLINE, nextIPPort {}",IPPort,nextIPPort);
                if (IPPort.equals(nextIPPort)){// 是最后一台下线
                    logger.info("only 1 dataNode {}, Skip the procedure",IPPort);
                    response = new HeartbeatResponse(false, dataNodeClientMap.size(),null);/// connection不再运行，不需要转移数据
                    break;
                }
                response = new HeartbeatResponse(false, dataNodeClientMap.size(),nextIPPort);// connection不再运行，全部数据转移给nextConnection
                 break;
            case RUNNING : // 在线，无变化
                logger.debug("{} RUNNING",request.getIPPort());
                response = new HeartbeatResponse(true, dataNodeClientMap.size(),null);//connection依然运行，不需要转移数据
                 break;
            default:
                logger.error("error status {}",request);
                response = new HeartbeatResponse(false, dataNodeClientMap.size(),null);// 非正常数据
        }
        return response;
    }

}
