package jazmin.server.im;

import java.io.IOException;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
/**
 * 
 * @author yama
 * 26 Dec, 2014
 */
@Sharable
public class IMMessageServerHandler extends ChannelHandlerAdapter{
	private static Logger logger=LoggerFactory.get(IMMessageServerHandler.class);
	private static final AttributeKey<IMSession> SESSION_KEY=
								AttributeKey.valueOf("s");
	private IMMessageServer messageServer;
	public IMMessageServerHandler(IMMessageServer messageServer) {
		this.messageServer=messageServer;
	}
	//
	@Override
    public void channelInactive(ChannelHandlerContext ctx) 
    		throws Exception {
    	IMSession session=ctx.channel().attr(SESSION_KEY).get();
    	messageServer.sessionDisconnected(session);
	}
	//
	@Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) 
    		throws Exception {
		IMRequestMessage reqMessage=(IMRequestMessage) msg;
		IMSession session=ctx.channel().attr(SESSION_KEY).get();
		messageServer.receiveMessage(session, reqMessage);
	}
	//
	@Override
	public void channelActive(ChannelHandlerContext ctx) 
			throws Exception {
		IMSession session=new IMSession(ctx.channel());
		ctx.channel().attr(SESSION_KEY).set(session);
		messageServer.sessionCreated(session);
	}
	//
	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
			throws Exception {
		if(evt instanceof IdleStateEvent){  
			IMSession session=ctx.channel().attr(SESSION_KEY).get();
			messageServer.sessionIdle(session);
        }    
	}
    //
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) 
    		throws Exception {
    	if(cause instanceof IOException){
    		logger.warn("exception on channal:"+ctx.channel()+","+cause.getMessage());
    	}else{
    		logger.error("exception on channal:"+ctx.channel(),cause);	
    	}
    	ctx.close();
    }
}
