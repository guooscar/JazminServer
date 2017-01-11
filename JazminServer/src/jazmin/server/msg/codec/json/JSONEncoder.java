/**
 * *****************************************************************************
 * 							Copyright (c) 2014 yama.
 * This is not a free software,all rights reserved by yama(guooscar@gmail.com).
 * ANY use of this software MUST be subject to the consent of yama.
 *
 * *****************************************************************************
 */
/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package jazmin.server.msg.codec.json;

import java.nio.charset.Charset;

import com.alibaba.fastjson.JSON;

import io.netty.channel.ChannelHandler.Sharable;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.misc.io.NetworkTrafficStat;
import jazmin.server.msg.codec.BinaryEncoder;
import jazmin.server.msg.codec.ResponseMessage;
import jazmin.util.DumpUtil;
/**
 * 
 * @author yama
 * 26 Dec, 2014
 */
@Sharable
public class JSONEncoder extends BinaryEncoder {
	private static Logger logger=LoggerFactory.get(JSONEncoder.class);
    private Charset charset;
    NetworkTrafficStat networkTrafficStat;
	public JSONEncoder(NetworkTrafficStat networkTrafficStat) {
		super(networkTrafficStat);
		charset=Charset.forName("UTF-8");
	}
	//
	@Override
	protected byte[] encode(ResponseMessage msg) throws Exception {
		//send raw data
    	if(msg.rawData!=null){
			return msg.rawData;
		}
    	String json=JSON.toJSONString(msg.responseMessages)+"\n";
    	if(logger.isDebugEnabled()){
    		logger.debug("\nencode message--------------------------------------\n"
						+DumpUtil.formatJSON(json));
    	}
		return json.getBytes(charset);
	}
}
