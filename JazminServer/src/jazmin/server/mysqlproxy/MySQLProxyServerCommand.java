package jazmin.server.mysqlproxy;

import java.util.List;

import jazmin.core.Jazmin;
import jazmin.server.console.ascii.TablePrinter;
import jazmin.server.console.builtin.ConsoleCommand;

/**
 * 
 * @author yama 26 Dec, 2014
 */
public class MySQLProxyServerCommand extends ConsoleCommand {
	private MySQLProxyServer server;

	public MySQLProxyServerCommand() {
		super(true);
		id = "mysqlproxy";
		desc = "mysqlproxy server ctrl command";
		addOption("rule", false, "show server rule.", this::showServerRule);
		addOption("s", false, "show server sessions.", this::showSessions);
		//
		server = Jazmin.getServer(MySQLProxyServer.class);
	}

	//
	@Override
	public void run() throws Exception {
		if (server == null) {
			out.println("can not find MySQLProxyServer.");
			return;
		}
		super.run();
	}

	//
	private void showServerRule(String args) {
		TablePrinter tp = TablePrinter.create(out).length(15, 50, 50)
				.headers("LOCALPORT", "REMOTEINFO", "AUTHPROVIDER");
		List<ProxyRule> channels = server.getRules();
		for (ProxyRule s : channels) {
			tp.print(s.localPort, s.remoteHost + ":" + s.remotePort,
					s.authProvider);
		}
	}

	//
	private void showSessions(String args) {
		TablePrinter tp = TablePrinter
				.create(out)
				.length(15, 40, 10, 15, 15, 15, 15, 10)
				.headers("ID", "DBINFO", "LOCALPORT", "DBUSER", "USER",
						"CREATETIME", "ACCTIME", "PKGCNT");
		List<ProxySession> channels = server.getSessions();
		for (ProxySession s : channels) {
			tp.print(s.id, s.remoteHost + ":" + s.remotePort, s.localPort + "",
					s.dbUser, s.user, formatDate(s.createTime),
					formatDate(s.lastAccTime), s.packetCount);
		}
	}
}
