/**
 * *****************************************************************************
 * 							Copyright (c) 2014 yama.
 * This is not a free software,all rights reserved by yama(guooscar@gmail.com).
 * ANY use of this software MUST be subject to the consent of yama.
 *
 * *****************************************************************************
 */
package jazmin.server.msg.codec.zjson;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.handler.codec.MessageToByteEncoder;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.misc.NetworkTrafficStat;
import jazmin.server.msg.codec.ResponseMessage;
import jazmin.server.msg.codec.ResponseProto;
import jazmin.util.DumpUtil;
import jazmin.util.IOUtil;

import com.alibaba.fastjson.JSON;

/**
 * 
 * @author yama
 * @date Jun 8, 2014
 */
@Sharable
public class ZJSONEncoder extends MessageToByteEncoder<ResponseMessage> {
	private static Logger logger=LoggerFactory.get(ZJSONEncoder.class);
	private static final int MAX_MESSAGE_LENGTH=1024*1024;
	NetworkTrafficStat networkTrafficStat;
	public ZJSONEncoder(NetworkTrafficStat networkTrafficStat) {
		this.networkTrafficStat=networkTrafficStat;
	}
	@Override
	protected void encode(
			ChannelHandlerContext ctx, 
			ResponseMessage msg,
			ByteBuf out) throws Exception {
		if(msg.rawData!=null){
			out.writeBytes(msg.rawData);
			return;
		}
		ResponseProto bean=new ResponseProto();
    	bean.d=(System.currentTimeMillis());
    	bean.ri=(msg.requestId);
    	bean.rsp=(msg.responseMessages);
    	bean.si=(msg.serviceId);
    	bean.sc=(msg.statusCode);
    	bean.sm=(msg.statusMessage);
    	String json=JSON.toJSONString(bean);
    	//
    	byte bb[]=json.getBytes("UTF-8");
		byte compressBytes[] = IOUtil.compress(bb);
		int dataLength=compressBytes.length;
		if(logger.isDebugEnabled()){
    		logger.debug("\nencode message--------------------------------------\n{}"
    				+ "\nzjson encode size:{}",
    				DumpUtil.formatJSON(json+"\n"),
					bb.length+"/"+compressBytes.length);
    	}
    	
		if(dataLength>MAX_MESSAGE_LENGTH){
			throw new CorruptedFrameException("message too long" + dataLength
					+ "/" + MAX_MESSAGE_LENGTH);
		}
		networkTrafficStat.outBound(dataLength);
		out.writeInt(dataLength);
		out.writeBytes(compressBytes);
	}
	
}
