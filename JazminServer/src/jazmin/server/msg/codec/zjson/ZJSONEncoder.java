/**
 * 
 */
package jazmin.server.msg.codec.zjson;

import com.alibaba.fastjson.JSON;

import io.netty.channel.ChannelHandler.Sharable;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.misc.NetworkTrafficStat;
import jazmin.server.msg.codec.BinaryEncoder;
import jazmin.server.msg.codec.ResponseMessage;
import jazmin.server.msg.codec.ResponseProto;
import jazmin.util.DumpUtil;
import jazmin.util.IOUtil;

/**
 * @author yama
 * 31 Mar, 2015
 */
@Sharable
public class ZJSONEncoder extends BinaryEncoder{
	private static Logger logger= LoggerFactory.get(ZJSONEncoder.class);
	//
	public ZJSONEncoder(NetworkTrafficStat networkTrafficStat) {
		super(networkTrafficStat);
	}

	@Override
	protected byte[] encode(ResponseMessage msg) throws Exception{
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
		if(logger.isDebugEnabled()){
    		logger.debug("\nencode message--------------------------------------\n{}"
    				+ "\nzjson encode size:{}",
    				DumpUtil.formatJSON(json+"\n"),
					bb.length+"/"+compressBytes.length);
    	}
    	return compressBytes;
	}
}
