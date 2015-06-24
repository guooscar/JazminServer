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
package jazmin.server.im.mobile;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.nio.charset.Charset;

import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.misc.io.NetworkTrafficStat;
import jazmin.server.im.IMResponseMessage;
import jazmin.util.DumpUtil;
/**
 * 
 * @author yama
 * 26 Dec, 2014
 */
@Sharable
public class MobileEncoder extends MessageToByteEncoder<IMResponseMessage> {
	private static Logger logger=LoggerFactory.get(MobileEncoder.class);
    private Charset charset;
    NetworkTrafficStat networkTrafficStat;
	public MobileEncoder(NetworkTrafficStat networkTrafficStat) {
		charset=Charset.forName("UTF-8");
		this.networkTrafficStat=networkTrafficStat;
	}
	//
    @Override
    protected void encode(ChannelHandlerContext ctx, IMResponseMessage msg, ByteBuf out) 
    		throws Exception {
    	//send raw data
    	if(logger.isDebugEnabled()){
    		String json=new String(msg.rawData,charset);
    		logger.debug("\nencode message--------------------------------------\n"
						+DumpUtil.formatJSON(json));
    	}
    	networkTrafficStat.outBound(msg.rawData.length);
		out.writeBytes(msg.rawData);
		return;
	}
}
