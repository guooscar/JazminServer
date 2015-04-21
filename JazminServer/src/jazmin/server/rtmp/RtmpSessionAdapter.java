/**
 * 
 */
package jazmin.server.rtmp;

/**
 * @author yama
 *
 */
public abstract class RtmpSessionAdapter implements RtmpSessionListener{

	@Override
	public void onConnect(RtmpSession session) throws Exception {
		
	}

	@Override
	public void onPlay(RtmpSession session, String streamName) throws Exception {
		
	}

	@Override
	public void onPublish(RtmpSession session, String streamName, String type)
			throws Exception {
		
	}

	@Override
	public void onUnpublish(RtmpSession session) throws Exception {
		
	}

	@Override
	public void onPause(RtmpSession session) {
		
	}

	@Override
	public void onClose(RtmpSession session) {
		
	}
	
}
