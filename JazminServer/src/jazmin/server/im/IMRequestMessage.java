/**
 * *****************************************************************************
 * 							Copyright (c) 2014 yama.
 * This is not a free software,all rights reserved by yama(guooscar@gmail.com).
 * ANY use of this software MUST be subject to the consent of yama.
 *
 * *****************************************************************************
 */
package jazmin.server.im;



/**
 * 
 * @author yama
 * @date Jun 5, 2014
 */
public class IMRequestMessage {
	//
	public boolean isBadRequest;
	/**
	 * 请求消息的request必须大于零
	 */
	//public int requestId;//requestId
	public int serviceId;//serviceId
	public byte []rawData;
	public IMRequestMessage() {
	}
	//
	@Override
	public String toString() {
		return serviceId+"";
	}
}
