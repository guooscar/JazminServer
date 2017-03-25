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
		public String password;
	}
	//
	HostInfo getHostInfo(String token);
}
