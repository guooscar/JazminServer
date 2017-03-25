/**
 * 
 */
package jazmin.server.webssh;

import java.io.IOException;
import java.io.OutputStream;

import jazmin.log.Logger;
import jazmin.log.LoggerFactory;

/**
 * @author yama
 *
 */
public class OutputStreamEndpoint implements PeerEndpoint{
	private static Logger logger=LoggerFactory.get(OutputStreamEndpoint.class);
	OutputStream os;
	public OutputStreamEndpoint(OutputStream os){
		this.os=os;
	}
	//
	@Override
	public void close() {
		try {
			os.close();
		} catch (IOException e) {
			logger.catching(e);
		}
	}

	@Override
	public void write(String msg) {
		try {
			os.write(msg.getBytes("UTF-8"));
		} catch (IOException e) {
			logger.catching(e);
		}
	}
	//
	@Override
	public String toString() {
		return "OS:"+os;
	}
}
