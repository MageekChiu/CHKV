package cn.mageek.datanode.handler;

import cn.mageek.common.model.DataRequest;
import cn.mageek.common.model.DataResponse;
import cn.mageek.common.model.LineType;
import cn.mageek.datanode.res.CommandFactory;
import cn.mageek.common.command.AbstractDataNodeCommand;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

import static cn.mageek.common.model.HeartbeatType.TRANSFERRING;
import static cn.mageek.datanode.main.DataNode.dataNodeStatus;

/**
 * 处理server接受到来自client的消息对象的handler，业务逻辑的核心
 * @author Mageek Chiu
 * @date 2018/3/10 0010:16:22
 */
public class BusinessHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(BusinessHandler.class);

    private static final Set<String> UPDATE_COMMAND = new HashSet<String>(){{
        add("SET");add("SETNX");add("EXPIRE");add("APPEND");add("INCRBY");add("INCR");
        add("DECRBY");add("DECR");add("DEL");
        //add("GET");add("KEYS");add("COMMAND");
    }};

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object obj) throws Exception {
        try {
            DataRequest dataRequest = (DataRequest)obj;//转换消息对象
            String commandString = dataRequest.getCommand();
            // 数据迁移中，禁止修改，可以查看
            if (dataNodeStatus.equals(TRANSFERRING) && UPDATE_COMMAND.contains(commandString)){
                DataResponse dataResponse = new DataResponse(
                        LineType.SINGLE_ERROR,"DATA TRANSFERRING",dataRequest.getID());
                ctx.writeAndFlush(dataResponse);//从当前位置往上找outBound
                return;
            }
            AbstractDataNodeCommand command = CommandFactory.getCommand(commandString);
            if(command==null){
                logger.error("error command: {}",dataRequest);
                return;
            }
//            logger.debug("command:{}",command.getClass().getName());
            logger.debug("dataRequest:{}",dataRequest);
            DataResponse dataResponse = command.receive(dataRequest);// 处理请求 获得响应
            dataResponse.setID(dataRequest.getID());//设置响应ID
            logger.debug("dataResponse:{}",dataResponse);
            ctx.writeAndFlush(dataResponse);//从当前位置往上找outBound

        }catch (Exception e){
            logger.error("parse data :{} , from: {} , error: ", obj,ctx.channel().remoteAddress(),e);
        }

    }

}
