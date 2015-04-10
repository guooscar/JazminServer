/**
 * 
 */
package jazmin.server.im;

import jazmin.util.DumpIgnore;


/**
 * @author yama
 * 26 Dec, 2014
 */
@DumpIgnore
public class IMContext {
	private boolean isFlush;
	private boolean isContinuation;
	private IMSession session;
	private IMResult result;
	private IMRequestMessage requestMessage;
	private IMMessageServer messageServer;
	//
	IMContext(IMMessageServer messageServer,
			IMSession session,
			IMRequestMessage requestMessage,
			boolean isContinuation){
		this.messageServer=messageServer;
		this.session=session;
		this.requestMessage=requestMessage;
		this.isContinuation=isContinuation;
		isFlush=false;
	}
	//--------------------------------------------------------------------------
	//public interface
	/**
	 * get service id
	 */
	public int getServiceId(){
		return requestMessage.serviceId;
	}
	/**
	 * return message server
	 */
	public IMMessageServer getServer(){
		return messageServer;
	}
	/**
	 * return session of this context.
	 */
	public IMSession getSession(){
		return session;
	}
	/**
	 * put object to client side.
	 */
	public void setResult(IMResult result){
		this.result=result;
	}
	/**
	 * flush context.
	 */
	public void flush(){
		if(isFlush){
			throw new IllegalStateException("context already flushed");
		}
		isFlush=true;
		if(result!=null){
			//write response
			IMResponseMessage rspMessage=new IMResponseMessage();
			rspMessage.serviceId=requestMessage.serviceId;
			rspMessage.rawData=result.rawBytes;
			session.sendMessage(rspMessage);
		}
	}
	//--------------------------------------------------------------------------
	//private method
	
	//
	void close(){
		if(!isContinuation){
			flush();
		}
	}
}
