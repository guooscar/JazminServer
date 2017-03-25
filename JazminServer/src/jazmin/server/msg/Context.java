/**						JAZMIN SERVER SOURCE FILE
--------------------------------------------------------------------------------
	     	  ___  _______  _______  __   __  ___   __    _ 		
		     |   ||   _   ||       ||  |_|  ||   | |  |  | |		
		     |   ||  |_|  ||____   ||       ||   | |   |_| |		
		     |   ||       | ____|  ||       ||   | |       |		
		  ___|   ||       || ______||       ||   | |  _    |		
		 |       ||   _   || |_____ | ||_|| ||   | | | |   |		
		 |__yama_||__| |__||_______||_|   |_||___| |_|  |__|	 
		 
--------------------------------------------------------------------------------
********************************************************************************
 							Copyright (c) 2015 yama.
 This is not a free software,all rights reserved by yama(guooscar@gmail.com).
 ANY use of this software MUST be subject to the consent of yama.

********************************************************************************
*/
package jazmin.server.msg;

import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.server.msg.codec.RequestMessage;
import jazmin.server.msg.codec.ResponseMessage;
import jazmin.util.DumpIgnore;

/**
 * @author yama
 * 26 Dec, 2014
 */
@DumpIgnore
public class Context {
	private static Logger logger=LoggerFactory.get(Context.class);
	//
	private boolean isFlush;
	private boolean isDisableResponse;
	private boolean isContinuation;
	private Session session;
	private Object responseObject;
	private byte rawData[];
	private RequestMessage requestMessage;
	private MessageServer messageServer;
	//
	Context(MessageServer messageServer,
			Session session,
			RequestMessage requestMessage,
			boolean isDisableResponse,
			boolean isContinuation){
		this.messageServer=messageServer;
		this.session=session;
		this.requestMessage=requestMessage;
		this.isDisableResponse=isDisableResponse;
		this.isContinuation=isContinuation;
		isFlush=false;
	}
	//--------------------------------------------------------------------------
	//public interface
	/**
	 * @return service id
	 */
	public String getServiceId(){
		return requestMessage.serviceId;
	}
	/**
	 * @return message server
	 */
	public MessageServer getServer(){
		return messageServer;
	}
	/**
	 * @return session of this context.
	 */
	public Session getSession(){
		return session;
	}
	/**
	 * put object to client side.
	 */
	public void ret(Object rsp){
		responseObject=rsp;
	}
	/**
	 * put raw byte date to client side
	 * @param bytes
	 */
	public void putRawData(byte []bytes){
		rawData=bytes;
	}
	/**
	 * flush context.
	 */
	public void flush(boolean error){
		if(isFlush){
			throw new IllegalStateException("context already flushed");
		}
		isFlush=true;
		if(!isDisableResponse&&!error){
			//write response
			ResponseMessage rspMessage=new ResponseMessage();
			rspMessage.requestId=requestMessage.requestId;
			rspMessage.responseObject=responseObject;
			rspMessage.serviceId=requestMessage.serviceId;
			if(rawData!=null){
				//if raw data is not null.change payload data type to raw
				rspMessage.rawData=rawData;
			}
			session.sendMessage(rspMessage);
		}
	}
	//private method
	public byte [] getRawBytes(){
		return requestMessage.rawData;
	}
	//--------------------------------------------------------------------------
	//
	Boolean  getBoolean(int idx){
		String ss=requestMessage.requestParameters[idx];
		return ss==null?null:Boolean.valueOf(ss);
	}
	String  getString(int idx){	
		String ss=requestMessage.requestParameters[idx];
		return ss;
	}
	Long  getLong(int idx){
		String ss=requestMessage.requestParameters[idx];
		return ss==null?null:Long.valueOf(ss);
	}
	Integer  getInteger(int idx){
		String ss=requestMessage.requestParameters[idx];
		return ss==null?null:Integer.valueOf(ss);
	}
	Short  getShort(int idx){
		String ss=requestMessage.requestParameters[idx];
		return ss==null?null:Short.valueOf(ss);
	}
	Float  getFloat(int idx){
		String ss=requestMessage.requestParameters[idx];
		return ss==null?null:Float.valueOf(ss);
	}
	Double  getDouble(int idx){
		String ss=requestMessage.requestParameters[idx];
		return ss==null?null:Double.valueOf(ss);
	}
	//
	void close(boolean error){
		if(!isContinuation){
			flush(error);
		}
	}
	//
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		if(!isFlush){
			logger.fatal("context did not call flush {}",requestMessage);
		}
	}
}
