/**
 * 
 */
package jazmin.server.msg.codec.zjson;

import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.misc.NetworkTrafficStat;
import jazmin.server.msg.codec.BinaryDecoder;
import jazmin.server.msg.codec.JSONRequestParser;
import jazmin.server.msg.codec.RequestMessage;
import jazmin.util.DumpUtil;
import jazmin.util.IOUtil;

/**
 * @author yama
 * 31 Mar, 2015
 */
public class ZJSONDecoder extends BinaryDecoder{
	private static Logger logger= LoggerFactory.get(ZJSONDecoder.class);
	//
	public ZJSONDecoder(NetworkTrafficStat networkTrafficStat) {
		super(networkTrafficStat);
	}
	//
	@Override
	protected RequestMessage decode(byte[] payload) throws Exception {
		byte[] bb = IOUtil.decompress(payload);
		String s = new String(bb, "UTF-8");
		RequestMessage reqMessage=JSONRequestParser.createSimpleRequestMessage(s);
		if (logger.isDebugEnabled()) {
			logger.debug("\ndecode message--------------------------------------\n{} "
					+ "\nzjson decode size:{}",
					DumpUtil.formatJSON(s),
					payload.length+"/"+bb.length);
		}
		return reqMessage;
	}
}
