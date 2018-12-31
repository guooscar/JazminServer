/**
 * 
 */
package jazmin.test.server.mysqlproxy;

import jazmin.core.Jazmin;
import jazmin.server.console.ConsoleServer;
import jazmin.server.mysqlproxy.Database;
import jazmin.server.mysqlproxy.MySQLProxyServer;
import jazmin.server.mysqlproxy.ProxyRule;
import jazmin.server.mysqlproxy.ProxyRuleAuthProvider;
import jazmin.server.mysqlproxy.ProxySession;
import jazmin.util.MD5Util;

/**
 * @author yama
 *
 */
public class TestMySQLProxyServer {

	public static void main(String[] args) {
		MySQLProxyServer server=new MySQLProxyServer();
		Jazmin.addServer(server);
		Jazmin.addServer(new ConsoleServer());
		Jazmin.start();
		//
		ProxyRule rule=new ProxyRule();
		rule.localPort=5050;
		rule.remoteHost="dbhost";
		rule.remotePort=8001;
		rule.authProvider=new ProxyRuleAuthProvider() {
			@Override
			public Database auth(ProxySession session) {
				if(!session.user.equals("yan")){
					return null;
				}
				String password="123";
				byte[] shaPassword=MD5Util.encodeSHA1Bytes(password.getBytes());
				if(!session.comparePassword(shaPassword)){
					return null;
				}
				return new Database("root","password");
			}
		};
		server.addRule(rule);
	}

}
