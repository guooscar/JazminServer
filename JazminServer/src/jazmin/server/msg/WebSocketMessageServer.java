/**
 * 
 */
package jazmin.server.msg;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.timeout.IdleStateHandler;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.server.msg.codec.JSONRequestParser;
import jazmin.server.msg.codec.RequestMessage;
import jazmin.server.msg.codec.ResponseMessage;
import jazmin.server.msg.codec.ResponseProto;
import jazmin.util.DumpUtil;

import com.alibaba.fastjson.JSON;

/**
 * @author yama
 * 25 Feb, 2015
 */
public class WebSocketMessageServer extends MessageServer{
	private static Logger logger=LoggerFactory.get(WebSocketMessageServer.class);
	//
	public WebSocketMessageServer() {
		super();
	}
	//
	@Override
	protected void initNettyServer(){
		nettyServer=new ServerBootstrap();
		channelInitializer=new WSMessageServerChannelInitializer();
		bossGroup = new NioEventLoopGroup(1);
		workerGroup = new NioEventLoopGroup();
		nettyServer.group(bossGroup, workerGroup)
		.channel(NioServerSocketChannel.class)
		.option(ChannelOption.SO_BACKLOG, 128)    
		.option(ChannelOption.SO_REUSEADDR, true)    
		.childOption(ChannelOption.TCP_NODELAY, true)
        .childOption(ChannelOption.SO_KEEPALIVE, true) 
		.childHandler(channelInitializer);
	}
	//--------------------------------------------------------------------------
	//
	class WSMessageServerChannelInitializer extends ChannelInitializer<SocketChannel>{
		@Override
		protected void initChannel(SocketChannel ch) throws Exception {
			ch.pipeline().addLast("idleStateHandler",new IdleStateHandler(idleTime,idleTime,0));
			ch.pipeline().addLast(new HttpServerCodec());
			ch.pipeline().addLast(new HttpObjectAggregator(65536));
			ch.pipeline().addLast(new WebSocketServerCompressionHandler());
			ch.pipeline().addLast(new WebSocketServerHandler(WebSocketMessageServer.this));
		}
	}
	//
	//--------------------------------------------------------------------------
	public RequestMessage decodeMessage(String s){
		RequestMessage reqMessage=JSONRequestParser.createRequestMessage(s);
     	if(logger.isDebugEnabled()){
     		logger.debug("\ndecode message--------------------------------------\n"
     						+DumpUtil.formatJSON(s));
     	}
    	networkTrafficStat.inBound(s.getBytes().length);
     	return reqMessage;
	}
	//
	public String encodeMessage(ResponseMessage msg){
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
        networkTrafficStat.outBound(json.getBytes().length);
    	return json;
	}
}
