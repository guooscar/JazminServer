/**
 * *****************************************************************************
 * 							Copyright (c) 2014 yama.
 * This is not a free software,all rights reserved by yama(guooscar@gmail.com).
 * ANY use of this software MUST be subject to the consent of yama.
 *
 * *****************************************************************************
 */
package jazmin.server.msg.codec;

import jazmin.util.DumpUtil;


/**
 * 
 * @author yama
 * @date Jun 5, 2014
 */
public class RequestMessage {
	public static final int MAX_PARAMETER_COUNT=16;
	//
	public boolean isBadRequest;
	/**
	 * 请求消息的request必须大于零
	 */
	public int requestId;//requestId
	public String serviceId;//serviceId
	public String[]requestParameters;
	public RequestMessage() {
		requestParameters=new String[MAX_PARAMETER_COUNT];
	}
	//
	@Override
	public String toString() {
		return DumpUtil.dump(this);
	}
}
