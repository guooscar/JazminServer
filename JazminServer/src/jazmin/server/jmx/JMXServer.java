package jazmin.server.jmx;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.management.ManagementFactory;
import java.rmi.registry.LocateRegistry;
import java.util.HashMap;
import java.util.Map;

import javax.management.remote.JMXAuthenticator;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import javax.security.auth.Subject;

import jazmin.core.Server;

/**
 * 
 * @author yama 16 Jan, 2015
 */
public class JMXServer extends Server {
	private JMXConnectorServer connectorServer;
	private int port = 9003;
	private String jmxAddr;
	private JazminJMXAuthenticator authenticator;

	public JMXServer() {
		authenticator = new JazminJMXAuthenticator();
	}

	/**
	 * @return the authenticator
	 */
	public JazminJMXAuthenticator getAuthenticator() {
		return authenticator;
	}

	//
	static class JazminJMXAuthenticator implements JMXAuthenticator {
		private Map<String, String> principalMap;

		public JazminJMXAuthenticator() {
			principalMap = new HashMap<String, String>();
		}

		//
		public void addPrincipal(String principal, String credentials) {
			principalMap.put(principal, credentials);
		}

		//
		@Override
		public Subject authenticate(Object obj) {
			if (principalMap.isEmpty()) {
				return new Subject();
			}
			String[] credentials = (String[]) obj;
			String user = credentials[0];
			String pwd = credentials[1];
			if (user == null || pwd == null) {
				throw new SecurityException("bad credentials");
			}
			String thePwd = principalMap.get(user);
			if (thePwd == null) {
				throw new SecurityException("can not find pwd");
			}
			if (!pwd.equals(thePwd)) {
				throw new SecurityException("bad credentials");
			}
			return new Subject();
		}
	}

	// --------------------------------------------------------------------------
	private void initMbeanRMIServer() throws Exception {
		LocateRegistry.createRegistry(port);
		jmxAddr = "service:jmx:rmi:///jndi/rmi://127.0.0.1:" + port + "/jmx";
		JMXServiceURL url = new JMXServiceURL(jmxAddr);
		Map<String, Object> env = new HashMap<String, Object>();
		env.put(JMXConnectorServer.AUTHENTICATOR, authenticator);
		connectorServer = JMXConnectorServerFactory.newJMXConnectorServer(url,
				env, ManagementFactory.getPlatformMBeanServer());
		connectorServer.start();
	}

	// --------------------------------------------------------------------------
	//
	@Override
	public void start() throws Exception {
		initMbeanRMIServer();
	}

	@Override
	public String info() {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		String format = "%-40s : %-60s\n";
		pw.printf(format, "JMXAddr", jmxAddr);
		return sw.toString();
	}

	@Override
	public void stop() throws Exception{
		if (connectorServer != null) {
			connectorServer.stop();
		}
	}
	//
	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @param port
	 *            the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}
}
