/**
 * 
 */
package jazmin.server.msg.codec.zjson;

import io.netty.channel.ChannelHandler.Sharable;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.misc.io.NetworkTrafficStat;
import jazmin.server.msg.codec.BinaryEncoder;
import jazmin.server.msg.codec.ResponseMessage;
import jazmin.util.DumpUtil;
import jazmin.util.IOUtil;

import com.alibaba.fastjson.JSON;

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
		String json=JSON.toJSONString(msg.responseMessages);
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
