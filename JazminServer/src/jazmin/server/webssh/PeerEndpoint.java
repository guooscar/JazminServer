package jazmin.server.webssh;
/**
 * 
 * @author yama
 *
 */
public interface PeerEndpoint {
	public void close();
	public void write(String msg);
}
