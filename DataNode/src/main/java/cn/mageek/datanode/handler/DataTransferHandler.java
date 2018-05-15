package cn.mageek.datanode.handler;

import cn.mageek.common.model.DataRequest;
import cn.mageek.common.model.DataResponse;
import cn.mageek.common.model.HeartbeatResponse;
import cn.mageek.common.model.LineType;
import cn.mageek.common.util.Decoder;
import cn.mageek.common.util.Encoder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static cn.mageek.common.res.Constants.offlineKey;
import static cn.mageek.common.res.Constants.offlineValue;
import static cn.mageek.common.res.Constants.pageSize;
import static cn.mageek.common.util.ConsistHash.getHash;
import static cn.mageek.datanode.main.DataNode.DATA_POOL;


/**
 * 数据迁移
 * @author Mageek Chiu
 * @date 2018/5/7 0007:12:41
 */
public class DataTransferHandler  extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(DataTransferHandler.class);
    private static final String SET = "SET";
    private AtomicInteger ok;
    private boolean isAll;
    private String dataNodeIPPort;
    private List<String> transList;// 响应等待队列

    public DataTransferHandler(String dataNodeIPPort,boolean isAll) {
        this.isAll = isAll;
        this.dataNodeIPPort = dataNodeIPPort;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("opened dataTransfer connection to: {}",ctx.channel().remoteAddress());
        dataTransfer(ctx.channel());// 连接成功就开始转移数据
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info("closed dataTransfer connection: {}",ctx.channel().remoteAddress());
        if (ok.get()>0){
            logger.info("dataTransfer connection closed, some failed,{}",transList);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf in = (ByteBuf) msg;
        List<DataResponse> responses = Decoder.bytesToDataResponse(in);
        responses.forEach((response)->{
            logger.debug("DataNode dataTransfer received : {}",response);
            if (LineType.SINGLE_RIGHT.equals(response.getLineType())){
                transList.remove(response.getID());// 转移成功，删除等待列表

                if(ok.decrementAndGet() == 0){//都收完了
                    if(transList.isEmpty()) logger.info("dataTransfer completed, all succeeded");
                    else logger.info("dataTransfer completed, some failed,{}",transList);
                    ctx.channel().close();//断开连接就好,dataTransfer自然结束
                    if (isAll){
                        DATA_POOL = null;// 可以下线了，整个DataNode下线
                    }
                }
                // 如果 没收完 或者 粘包导致数量没解析够 或者 某条数据确实没有转移成功，该channel就会超时，但是请求端不会报超时只会触发inactive，接收端才会报
            }
        });
        in.release();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("connection to: {}，error: ",ctx.channel().remoteAddress(),cause);
        ctx.close();
    }

    private void dataTransfer(Channel channel) throws InterruptedException {

        int allNum =  DATA_POOL.size();
        List<DataRequest> requests = new ArrayList<>(allNum);
        transList = new ArrayList<>(allNum);

        if (isAll){// 转移全部数据给下一个节点
            DATA_POOL.forEach((k,v)->{
                DataRequest r = new DataRequest(SET,k,v);
                requests.add(r);
                transList.add(r.getID());
            });
        }else {// 转移部分数据给上一个节点
            int serverHash = getHash(dataNodeIPPort);
            DATA_POOL.forEach((k,v)->{
                int keyHash = getHash(k);
                if (keyHash <= serverHash){// 需要转移
                    DataRequest r = new DataRequest(SET,k,v);
                    requests.add(r);
                    transList.add(r.getID());
                }
            });
        }

        int listSize = requests.size();
        int transTime = (int) Math.ceil(listSize/pageSize);// 转移次数
//        ok = new AtomicInteger(transTime) ;
        ok = new AtomicInteger(listSize) ;
        logger.info("all data:{}, transfer data :{},pageSize:{},transfer time: {}",allNum,listSize,pageSize,transTime);

        for (int i = 0 ; i < transTime;i++){
            List<DataRequest> requests1 = new ArrayList<>((int) pageSize);
            int index;// 转移数据的索引
            for (int j = 0; j < pageSize ; j++){
                index = (int) (i*pageSize+j);
                if (index<listSize){// 最后一页可能不够
                    DataRequest dataRequest = requests.get(index);
                    logger.debug("转移数据: {}",dataRequest);
                    requests1.add(dataRequest);
                }
            }

            ByteBuf buf = Encoder.dataRequestToBytes(requests1);// 批量转移
            Thread.sleep(300);// 休息一段时间 ms 再继续发送
            int num = buf.readableBytes();
            ChannelFuture f = channel.writeAndFlush(buf);
            f.addListener((ChannelFutureListener) cf -> {
                logger.debug("sent buf length:{}",num);
//                if(ok.decrementAndGet() == 0){//都发完了
//                    Thread.sleep(8000);// 再等待一下，确保响应也都收完了// 实际上这里是不能确定的，只能说8000ms内没回复就算超时失败了。// 这样是不对的，因为是同一个线程，sleep会导致还有些response收不到
//
//                    if(transList.isEmpty()) logger.info("dataTransfer completed, all succeeded");
//                    else logger.info("dataTransfer completed, some failed,{}",transList);
//
//                    channel.close();//断开连接就好,dataTransfer自然结束
//
//                    if (isAll){
//                        DATA_POOL = null;// 可以下线了，整个DataNode下线
//                    }
//                }
            });
        }


    }

}
