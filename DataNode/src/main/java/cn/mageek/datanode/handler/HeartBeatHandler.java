package cn.mageek.datanode.handler;

import cn.mageek.common.model.HeartbeatResponse;
import cn.mageek.datanode.job.DataTransfer;
import cn.mageek.datanode.res.JobFactory;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;

import static cn.mageek.common.res.Constants.offlineKey;
import static cn.mageek.common.res.Constants.offlineValue;

/**
 * @author Mageek Chiu
 * @date 2018/5/7 0007:13:52
 */
public class HeartBeatHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(HeartBeatHandler.class);

    private String clientIP;
    private String clientPort;
    private Map<String,String> DATA_POOL ;// 数据存储池

    public HeartBeatHandler(String clientIP, String clientPort, Map<String, String> DATA_POOL) {
        this.clientIP = clientIP;
        this.clientPort = clientPort;
        this.DATA_POOL = DATA_POOL;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("opened connection to: {}",ctx.channel().remoteAddress());
//        long memoryAvailable = Runtime.getRuntime().freeMemory();
////        long cpuAvailable = Runtime.getRuntime().availableProcessors();
//        HeartbeatRequest request = new HeartbeatRequest(clientIP+":"+clientPort,memoryAvailable);
////        ByteBuf buf = Encoder.heartbeatRequestToBytes(request);
////        ctx.writeAndFlush(Unpooled.copiedBuffer(buf));
//        logger.debug("DataNode sent: " + request);
//        ctx.writeAndFlush(request);// 因为这个in 上面的 out有decoder，所以可以直接发送对象，而不需要自己再写一遍转为bytebuf的encoder
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info("closed connection: {}",ctx.channel().remoteAddress());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        HeartbeatResponse response = (HeartbeatResponse) msg;// 因为这个in 上面的 in 是decoder，所以直接可以获得对象
        logger.debug("DataNode received: {}",response);
//        ctx.close();// 收到响应就关闭连接
        handleResponse(response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("connection to: {}，error: ",ctx.channel().remoteAddress(),cause);
        ctx.close();
    }

    private void handleResponse(HeartbeatResponse response){
        DataTransfer dataTransfer = null;
        if (response.isOk()){// 继续运行
            if (response.getIPPort()!=null){
                logger.info("DataNode 需要转移部分数据给上一个节点");
                dataTransfer = (DataTransfer) JobFactory.getJob("DataTransfer");
                dataTransfer.connect(response.getIPPort(),false);
            }else{
                logger.debug("DataNode不需要转移数据");
            }
        }else{
            if (response.getIPPort()!=null){
                logger.info("DataNode 不再运行，数据全部迁移给下一个节点");
                dataTransfer = (DataTransfer) JobFactory.getJob("DataTransfer");
                dataTransfer.connect(response.getIPPort(),true);
            }else{
                logger.debug("DataNode 最后一台下线，不需要转移数据");
                DATA_POOL.put(offlineKey,offlineValue);// 下线
            }
        }

        if(dataTransfer != null){
            Thread transfer = new Thread(dataTransfer,"dataTransfer");
            transfer.start();// 新起一个线程，但是这样可能不稳定，待改进
        }
    }

}
