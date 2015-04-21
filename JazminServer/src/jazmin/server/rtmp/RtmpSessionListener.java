/**
 * 
 */
package jazmin.server.rtmp;

/**
 * @author yama
 *
 */
public interface RtmpSessionListener {
	void onConnect(RtmpSession session)throws Exception;
	void onPlay(RtmpSession session,String streamName)throws Exception;
	void onPublish(RtmpSession session,String streamName,String type)throws Exception;
	void onUnpublish(RtmpSession session)throws Exception;
	void onPause(RtmpSession session);
	void onClose(RtmpSession session);
}
