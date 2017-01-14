package jazmin.server.webssh;

/**
 * 
 * @author yama
 *
 */
public interface HostInfoProvider {
	public static class HostInfo{
		public String host;
		public int port;
		public String user;
		public String password;
		public String cmd;
		public boolean enableInput;
	}
	//
	HostInfo getHostInfo(String token);
}
