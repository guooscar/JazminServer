package jazmin.server.websockify;

/**
 * 
 * @author yama
 *
 */
public interface HostInfoProvider {
	public static class HostInfo{
		public String host;
		public int port;
	}
	//
	HostInfo getHostInfo(String token);
}
