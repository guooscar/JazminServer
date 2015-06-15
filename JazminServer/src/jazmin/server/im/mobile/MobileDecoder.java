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
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;

import java.nio.charset.Charset;

import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.misc.io.NetworkTrafficStat;
import jazmin.server.im.IMRequestMessage;
import jazmin.util.DumpUtil;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * 
 */
public class MobileDecoder extends DelimiterBasedFrameDecoder {
	private static Logger logger=LoggerFactory.get(MobileDecoder.class);
	private Charset charset;
    NetworkTrafficStat networkTrafficStat;
	public MobileDecoder(NetworkTrafficStat networkTrafficStat) {
		super(3000,Unpooled.copiedBuffer(new byte[]{'\r','\n'}));
		this.networkTrafficStat=networkTrafficStat;
		charset=Charset.forName("UTF-8");
	}
    @Override
    protected  Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf decoded = (ByteBuf) super.decode(ctx, in);
        if (decoded == null) {
        	return decoded;
        }
     	String s=decoded.toString(charset);
     	if(logger.isDebugEnabled()){
     		logger.debug("\ndecode message--------------------------------------\n"
     						+DumpUtil.formatJSON(s));
     	}
     	IMRequestMessage reqMessage=new IMRequestMessage();
     	reqMessage.isBadRequest=false;
     	reqMessage.rawData=s.getBytes(charset);
     	JSONObject reqObj=JSON.parseObject(s);
     	reqMessage.mobileId=reqObj.getString("msgType");
     	networkTrafficStat.inBound(s.getBytes().length);
       	return (reqMessage);
    }
  
}
