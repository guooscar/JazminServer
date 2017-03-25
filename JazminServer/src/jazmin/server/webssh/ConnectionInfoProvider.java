package jazmin.server.webssh;

/**
 * 
 * @author yama
 *
 */
public interface ConnectionInfoProvider {
	public static class ConnectionInfo{
		public String name;
		public String host;
		public int port;
		public String user;
		public String password;
		public boolean enableInput;
		public ChannelListener channelListener;
		//
		@Override
		public String toString() {
			return user+"@"+host+":"+port+"/"+"input:"+enableInput+"/"+channelListener;
		}
	}
	//
	ConnectionInfo getConnectionInfo(String token);
}
