/**
 * 
 */
package jazmin.server.console;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jazmin.core.Jazmin;
import jazmin.core.Server;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.misc.InfoBuilder;
import jazmin.server.console.repl.CliRunnerCommandFactory;

import org.apache.sshd.SshServer;
import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;

/**
 * @author yama
 * 26 Dec, 2014
 */
public class ConsoleServer extends Server{
	private static Logger logger=LoggerFactory.get(ConsoleServer.class);
	//
	int port=2222;
	SshServer sshServer;
	Authenticator authenticator;
	Map<String,ConsoleCommand>commands=new HashMap<String, ConsoleCommand>();
	//
	private void startSshServer()throws Exception {
		sshServer= SshServer.setUpDefaultServer();
		sshServer.setNioWorkers(2);//we just need 2 nio worker for service
		sshServer.setPort(port);
		sshServer.setKeyPairProvider(new SimpleGeneratorHostKeyProvider("jch.ser"));
		sshServer.setPasswordAuthenticator(new PasswordAuthenticator() {
            public boolean authenticate(String u, String p, ServerSession session) {
            	String loginUser=u;
            	session.setUsername(loginUser);
            	InetSocketAddress sa=(InetSocketAddress)session.getIoSession().getRemoteAddress();
            	String loginHostAddress=sa.getAddress().getHostAddress();
            	logger.warn("console server login {}@{}",loginUser,loginHostAddress);
            	if(authenticator!=null){
            		return authenticator.auth(u,p);
            	}else{
            		return true;
            	}
            }
        });
        //
        loadCommand();
        //
        resetBanner();
        sshServer.setShellFactory(new CliRunnerCommandFactory(commands));
        sshServer.start();
    }
	//
	public List<ConsoleCommand>commands(){
		return new ArrayList<ConsoleCommand>(commands.values());
	}
	//
	private void resetBanner() {
		String welcomeMsg =
		"----------------------------------------------------------------------\r\n"
		+ "\r\n"
		+ Jazmin.LOGO
		+ "\r\n"
		+ "\t\tWelcome to JazminServer\r\n"
		+ "\t\ttype 'help' for more information.\r\n"
		+ "----------------------------------------------------------------------\r\n";
		//
		sshServer.getProperties().put(SshServer.WELCOME_BANNER, welcomeMsg);
	}
	//
	private void loadCommand(){
		//
		registerCommand(new JazCommand());
		registerCommand(new JazminCommand());
		registerCommand(new VMCommand());
		registerCommand(new WhoCommand());
		registerCommand(new HistoryCommand());
		registerCommand(new HelpCommand());
		registerCommand(new EchoCommand());
		registerCommand(new GrepCommand());
		registerCommand(new SortCommand());
		registerCommand(new HeadCommand());
		registerCommand(new TailCommand());
		registerCommand(new WcCommand());
		registerCommand(new LessCommand());
		registerCommand(new UniqCommand());
		registerCommand(new ManCommand());
	}
	//--------------------------------------------------------------------------
	/**
	 * register command to console server
	 * @param cmd
	 */
	public void registerCommand(ConsoleCommand cmd){
		if(commands.containsKey(cmd.getId())){
			throw new IllegalArgumentException("cmd :"+cmd.getId()+" already exists.");
		}
		commands.put(cmd.getId(), cmd);
	}
	//
	public ConsoleCommand getCommand(String name){
		return (ConsoleCommand)commands.get(name);
	}
	//
	/**
	 * 
	 * @param a
	 */
	public void setAuthenticator(Authenticator a){
		this.authenticator=a;
	}
	//
	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @param port the port to set
	 */
	public void setPort(int port) {
		if(isStarted()){
			throw new IllegalStateException("set before started.");
		}
		this.port = port;
	}
	//--------------------------------------------------------------------------
	@Override
	public void start() throws Exception {
		startSshServer();
	}
	//
	@Override
	public void stop()throws Exception {
		if(sshServer!=null){
			sshServer.stop();
		}
	}

	@Override
	public String info() {
		InfoBuilder ib= InfoBuilder.create()
				.format("%-30s:%-30s\n")
				.print("port",port)
				.print("authenticator",authenticator);
		ib.section("commands");
		commands.forEach((s,cmd)->{
			ib.print(s,cmd.getClass().getName());
		});
		return ib.toString();
	}
}
