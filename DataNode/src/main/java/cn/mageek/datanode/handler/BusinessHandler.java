package cn.mageek.datanode.handler;

import cn.mageek.common.model.DataRequest;
import cn.mageek.common.model.DataResponse;
import cn.mageek.datanode.res.CommandFactory;
import cn.mageek.common.command.AbstractDataNodeCommand;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 处理server接受到来自client的消息对象的handler，业务逻辑的核心
 * @author Mageek Chiu
 * @date 2018/3/10 0010:16:22
 */
public class BusinessHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(BusinessHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object obj) throws Exception {
        try {
            DataRequest dataRequest = (DataRequest)obj;//转换消息对象
            AbstractDataNodeCommand command = CommandFactory.getCommand(dataRequest.getCommand());
            if(command==null){
                logger.error("error command: {}",dataRequest);
                return;
            }
//            logger.debug("command:{}",command.getClass().getName());
            logger.debug("dataRequest:{}",dataRequest);
            DataResponse dataResponse = command.receive(dataRequest);//接受消息并处理
            if (dataResponse!=null){//如果需要响应操作
                 ctx.writeAndFlush(dataResponse);//从当前位置往上找outBound
//                 ctx.channel().writeAndFlush(dataResponse);//从最底部找outBound
            }
        }catch (Exception e){
            logger.error("parse data :{} , from: {} , error: ", obj,ctx.channel().remoteAddress(),e);
        }

    }

}
