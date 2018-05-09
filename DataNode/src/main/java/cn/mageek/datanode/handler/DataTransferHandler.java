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

import static cn.mageek.common.res.Constants.pageSize;
import static cn.mageek.common.util.ConsistHash.getHash;


/**
 * 数据迁移
 * @author Mageek Chiu
 * @date 2018/5/7 0007:12:41
 */
public class DataTransferHandler  extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(DataTransferHandler.class);

    private Map<String,String> DATA_POOL ;// 数据存储池
    private boolean isAll;
    private String dataNodeIPPort;
//    private AtomicInteger okNumber;


    public DataTransferHandler(String dataNodeIPPort,Map<String, String> DATA_POOL,boolean isAll) {
        this.DATA_POOL = DATA_POOL;
        this.isAll = isAll;
        this.dataNodeIPPort = dataNodeIPPort;
    }
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("opened connection to: {}",ctx.channel().remoteAddress());
//        okNumber = new AtomicInteger(0);// 置零
        dataTransfer(ctx.channel());// 连接成功就开始转移数据
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info("closed connection: {}",ctx.channel().remoteAddress());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf in = (ByteBuf) msg;
//        logger.debug("DataNode DataTransferHandler received buffer: {}",in.toString(CharsetUtil.UTF_8));// 是一条一条的，没有粘在一起,但是是几乎同时出现的
        DataResponse response = Decoder.bytesToDataResponse(in);
        logger.debug("DataNode DataTransferHandler received : {}",response);

        in.release();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("connection to: {}，error: ",ctx.channel().remoteAddress(),cause);
        ctx.close();
    }

    private void dataTransfer(Channel channel) throws InterruptedException {

        List<DataRequest> requests = new ArrayList<>();
        String SET = "SET";
        if (isAll){// 转移全部数据给下一个节点
            DATA_POOL.forEach((k,v)->{
                requests.add(new DataRequest(SET,k,v));
//                okNumber.incrementAndGet();
            });
            DATA_POOL = null;//释放
        }else {// 转移部分数据给上一个节点
            int serverhash = getHash(dataNodeIPPort);
            DATA_POOL.forEach((k,v)->{
                int keyHash = getHash(k);
                if (keyHash <= serverhash){// 需要转移
                    requests.add(new DataRequest(SET,k,v));
//                    okNumber.incrementAndGet();
                }
            });
        }

        int listSize = requests.size();
        int transTime = (int) Math.ceil(listSize/pageSize);// 转移次数
        AtomicInteger ok = new AtomicInteger(transTime) ;
        logger.debug("all data:{}, transfer data :{},pageSize:{},transfer time: {}",DATA_POOL.size(),listSize,pageSize,transTime);
        for (int i = 0 ; i < transTime;i++){
            List<DataRequest> requests1 = new ArrayList<>((int) pageSize);
            for (int j = 0; j < pageSize ; j++){
                int index = (int) (i*pageSize+j);
                if (index<listSize){
                    DataRequest dataRequest = requests.get(index);
                    logger.debug("转移数据: {}",dataRequest);
                    requests1.add(dataRequest);
                }
            }
            // 实测，发得太快不会粘包，接收端能一个一个解析，但是由于每收到一个包就回复，可能导致回复太快粘成了一个巨大的包而被发送端
            // io.netty.handler.codec.TooLongFrameException: Adjusted frame length exceeds 2048: 726616849 - discarded
            // 一条一条的发也不行，而且每次都是 Adjusted frame length exceeds 2048: 726616849 - discarded 猜测不是长度原因而是解码的原因，异常栈里也有ObjectDecoder，先注释掉看看
            // 终于明白，这个 DataTransfer 不需要ObjectDecoder 和 ObjectEncoder 因为我自己已经写了编解码的function
            ByteBuf buf = Encoder.dataRequestToBytes(requests1);// 批量转移
            Thread.sleep(500);// 所以休息一段时间 ms 再继续发送
            int num = buf.readableBytes();
            ChannelFuture f = channel.writeAndFlush(buf);
            f.addListener((ChannelFutureListener) channelFuture -> {
                logger.debug("successfully sent buf length:{}",num);
                if(ok.decrementAndGet() == 0){// 尽力而为即可，不去校验转移的正确性，若要校验就得去全部的response中一个一个查看，有error就重发，超时没有回复完毕就只能全部重发，因为对不上，不知道那个对错
                    logger.info("dataTransfer completed");
                    Thread.sleep(5000);// 等待完成通信
                    if (isAll){
                        DATA_POOL = null ; //可以下线了，整个DataNode下线
                    }else {
                        channel.close();//断开连接就好,dataTransfer自然结束
                    }
                }
            });
        }


    }

}
