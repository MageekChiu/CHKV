package cn.mageek.namenode.handler;

import cn.mageek.common.model.HeartbeatRequest;
import cn.mageek.common.model.HeartbeatResponse;
import cn.mageek.common.util.ConsistHash;
import cn.mageek.namenode.main.NameNode;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.atomic.AtomicReference;

import static cn.mageek.common.model.HeartbeatType.*;
import static cn.mageek.namenode.main.NameNode.dataNodeClientMap;
import static cn.mageek.namenode.main.NameNode.dataNodeMap;
import static cn.mageek.namenode.main.NameNode.sortedServerMap;

/**
 * @author Mageek Chiu
 * @date 2018/5/7 0007:13:52
 */
public class DataNodeHeartBeatHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(DataNodeHeartBeatHandler.class);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        String connection = ctx.channel().remoteAddress().toString();
        // 维护集合
        dataNodeMap.put(connection,ctx.channel());
        logger.info("new connection arrived: {}, DataNode living {}",connection, dataNodeMap.size());//包含ip:port
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String connection = ctx.channel().remoteAddress().toString();
        // 维护集合
        dataNodeMap.remove(connection);//
        logger.info("connection closed: {}, DataNode living {}",connection,dataNodeMap.size());

        String IPPort = dataNodeClientMap.get(connection);
        int hash = ConsistHash.getHash(IPPort);
        if (sortedServerMap.containsKey(hash)){//该节点未发送OFFLINE请求就下线了
            logger.error("DataNode {} get offline before request from NameNode, IPPort {}",connection,IPPort);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String connection = ctx.channel().remoteAddress().toString();
        HeartbeatRequest request = (HeartbeatRequest)msg ;
//        logger.debug("NameNode received: {}" , request);
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
//        ConsistHash hash = new ConsistHash(sortedServerMap,dataNodeClientMap);// 此时dataNodeClientMap是最新的，据此更新sortedServerMap// 频繁创建对象不合适，还是换成静态函数比较好
        switch (request.getStatus()){
            case ONLINE : // 上线，要通知环中下一台dataNode分一部分数据给本个dataNode
                // 维护集合，加入
                IPPort = request.getIPPort();
                dataNodeClientMap.put(connection,IPPort);
                ConsistHash.circleAdd(sortedServerMap,IPPort);// 将IPPort加入hash环sortedServerMap
                NameNode.dataNodeChanged = true;// 维护后后发出通知

                nextIPPort = ConsistHash.getServer(sortedServerMap,IPPort,true);// 根据hash环找到下一台服务器
//                 根据nextIPPort找到nextConnection，再找到nextChannel，然后就可以发消息了
                AtomicReference<String> nextConnection = new AtomicReference<>("");
                dataNodeClientMap.forEach((k,v)->{
                    if (v.equals(nextIPPort)) nextConnection.set(k);
                });
                logger.debug("{} ONLINE, nextIPPort {}, nextConnection {}",IPPort,nextIPPort,nextConnection.get());
                response = new HeartbeatResponse(true, dataNodeClientMap.size(),null);//connection依然运行，不需要转移数据
                if (IPPort.equals(nextIPPort)){// 是第一台上线，跳过处理步骤
                    logger.info("only 1 dataNode {}, Skip the procedure",IPPort);
                    break;
                 }
                 // 否则就不是第一台上线，给下一个节点发消息
                 Channel nextChannel = dataNodeMap.get(nextConnection.get());
                 if (nextChannel != null){// 正常
                     HeartbeatResponse response1 = new HeartbeatResponse(true, dataNodeClientMap.size(),IPPort);// nextConnection依然运行，只是分一部分数据给connection
                     logger.debug("NameNode pushed : {} to {}" ,response1,connection);
                     nextChannel.writeAndFlush(response1);
                 }else {// 下一个节点已经下线，但是没有维护hash环。也就是该节点未发送OFFLINE请求就下线了，属于失效
                     logger.error("next DataNode get offline before request from NameNode");
                 }
                 break;

            case OFFLINE : // 下线，要通知本个dataNode把数据全部转移至环中下一台dataNode
                 // 维护集合，删除
                 IPPort = request.getIPPort();
                dataNodeClientMap.remove(connection);
                 ConsistHash.cirlceDel(sortedServerMap,IPPort);
                 NameNode.dataNodeChanged = true;// 维护后发出通知

                 nextIPPort = ConsistHash.getServer(sortedServerMap,IPPort,true);
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
