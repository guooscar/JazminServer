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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.nio.CharBuffer;
import java.nio.charset.Charset;

import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.misc.NetworkTrafficStat;
import jazmin.server.msg.codec.ResponseMessage;
import jazmin.util.DumpUtil;

import com.alibaba.fastjson.JSON;
/**
 * 
 * @author yama
 * 26 Dec, 2014
 */
@Sharable
public class JSONEncoder extends MessageToByteEncoder<ResponseMessage> {
	private static Logger logger=LoggerFactory.get(JSONEncoder.class);
    private Charset charset;
    NetworkTrafficStat networkTrafficStat;
	public JSONEncoder(NetworkTrafficStat networkTrafficStat) {
		charset=Charset.forName("UTF-8");
		this.networkTrafficStat=networkTrafficStat;
	}
	//
    @Override
    protected void encode(ChannelHandlerContext ctx, ResponseMessage msg, ByteBuf out) 
    		throws Exception {
    	//send raw data
    	if(msg.rawData!=null){
			out.writeBytes(msg.rawData);
			return;
		}
    	//
    	ResponseProto bean=new ResponseProto();
    	bean.d=(System.currentTimeMillis());
    	bean.ri=(msg.requestId);
    	bean.rsp=(msg.responseMessages);
    	bean.si=(msg.serviceId);
    	bean.sc=(msg.statusCode);
    	bean.sm=(msg.statusMessage);
    	String json=JSON.toJSONString(bean)+"\n";
    	if(logger.isDebugEnabled()){
    		logger.debug("\nencode message--------------------------------------\n"
						+DumpUtil.formatJSON(json));
    	}
    	ByteBuf result=ByteBufUtil.encodeString(ctx.alloc(), CharBuffer.wrap(json), charset);
        out.writeBytes(result);
        networkTrafficStat.outBound(json.getBytes().length);
    }
}
