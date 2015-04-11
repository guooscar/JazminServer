/**
 * 
 */
package jazmin.server.im;

/**
 * @author g2131
 *
 */
public class IMResult {
	public IMResult() {
	}
	public IMResult(byte[]bb) {
		this.rawBytes=bb;
	}
	
	public byte[]rawBytes;
}
