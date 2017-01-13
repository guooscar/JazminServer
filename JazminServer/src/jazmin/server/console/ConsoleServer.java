/**
 * 
 */
package jazmin.server.console;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.sshd.common.config.keys.KeyUtils;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;

import jazmin.core.Jazmin;
import jazmin.core.Server;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.misc.InfoBuilder;
import jazmin.server.console.builtin.ConsoleCommand;
import jazmin.server.console.builtin.CutCommand;
import jazmin.server.console.builtin.DateCommand;
import jazmin.server.console.builtin.EchoCommand;
import jazmin.server.console.builtin.GrepCommand;
import jazmin.server.console.builtin.HeadCommand;
import jazmin.server.console.builtin.HelpCommand;
import jazmin.server.console.builtin.HistoryCommand;
import jazmin.server.console.builtin.JazCommand;
import jazmin.server.console.builtin.JazminCommand;
import jazmin.server.console.builtin.LessCommand;
import jazmin.server.console.builtin.ManCommand;
import jazmin.server.console.builtin.NlCommand;
import jazmin.server.console.builtin.SortCommand;
import jazmin.server.console.builtin.TRCommand;
import jazmin.server.console.builtin.TailCommand;
import jazmin.server.console.builtin.UniqCommand;
import jazmin.server.console.builtin.UpTimeCommand;
import jazmin.server.console.builtin.VMCommand;
import jazmin.server.console.builtin.WcCommand;
import jazmin.server.console.builtin.WhoCommand;
import jazmin.server.console.repl.CliRunnerCommandFactory;

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
	LinkedList<String>commandHistory=new LinkedList<String>();
	int maxCommandHistory;
	SimplePasswordAuthenticator defaultAuthenticator;
	//
	private void startSshServer()throws Exception {
		maxCommandHistory=500;
		sshServer= SshServer.setUpDefaultServer();
		//sshServer.
		sshServer.setNioWorkers(2);//we just need 2 nio worker for service
		sshServer.setPort(port);
		String jchPath=Jazmin.getServerPath()+"/"+"jch.ser";
		SimpleGeneratorHostKeyProvider keyProvider=new SimpleGeneratorHostKeyProvider(new File(jchPath));
		keyProvider.setAlgorithm(KeyUtils.RSA_ALGORITHM);
		sshServer.setKeyPairProvider(keyProvider);
		defaultAuthenticator=new SimplePasswordAuthenticator();
		defaultAuthenticator.setUser("jazmin", "jazmin");
		authenticator=defaultAuthenticator;
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
		loadCommandHistory();
        //
        loadCommand();
        //
        resetBanner();
        sshServer.setShellFactory(new CliRunnerCommandFactory(this,commands));
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
		registerCommand(new NlCommand());
		registerCommand(new DateCommand());
		registerCommand(new UpTimeCommand());
		registerCommand(new CutCommand());
		registerCommand(new TRCommand());
		
	}
	//--------------------------------------------------------------------------
	void loadCommandHistory(){
		String historyFilePath=Jazmin.getServerPath()+"/.console-history";
		File historyFile=new File(historyFilePath);
		if(historyFile.exists()){
			try {
				Files.lines(historyFile.toPath()).forEach((s)->{
					addCommandHistory(s);
				});
			} catch (IOException e) {
				logger.warn("can not creat read history file {}",historyFile);
			}
		}
	}
	//
	void saveCommandHistory()throws Exception{
		String historyFilePath=Jazmin.getServerPath()+"/.console-history";
		File historyFile=new File(historyFilePath);
		if(!historyFile.exists()){
			if(!historyFile.createNewFile()){
				logger.error("can not create {} file",historyFilePath);
				return;
			}
		}
		try(BufferedWriter historyWriter=new BufferedWriter(new FileWriter(historyFile,false));){
			for(String s:commandHistory){
				historyWriter.write(s+"\n");
			}
			historyWriter.flush();
			historyWriter.close();
		}
	}
	//
	public void addCommandHistory(String line){
		commandHistory.add(line);
		if(commandHistory.size()>maxCommandHistory){
			commandHistory.removeFirst();
		}
	}
	public void clearHistory(){
		commandHistory.clear();
	}
	//
	public List<String>getCommandHistory(){
		return new LinkedList<String>(commandHistory);
	}
	//
	/**
	 * register command to console server
	 * @param cmd
	 */
	public void registerCommand(Class<? extends ConsoleCommand> commandClass){
		try {
			registerCommand(commandClass.newInstance());
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		} 
	}
	//
	private void registerCommand(ConsoleCommand cmd){
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
	public List<ConsoleCommand>getCommands(){
		return new ArrayList<ConsoleCommand>(commands.values());
	}
	//
	public List<ConsoleSession>getConsoleSession(){
		List<ConsoleSession>sessions=new ArrayList<ConsoleSession>();
		sshServer.getActiveSessions().forEach(session->{
    		InetSocketAddress sa=(InetSocketAddress)session.getIoSession().getRemoteAddress();
        	String loginHostAddress=sa.getAddress().getHostAddress();
        	ConsoleSession s=new ConsoleSession();
        	s.user= session.getUsername();
        	s.remoteHost=loginHostAddress;
        	s.remotePort=sa.getPort();
        	sessions.add(s);
    	});
		return sessions;
	}
	
	/**
	 * @return the defaultAuthenticator
	 */
	public SimplePasswordAuthenticator getDefaultAuthenticator() {
		return defaultAuthenticator;
	}
	/**
	 * 
	 * @param a
	 */
	public void setAuthenticator(Authenticator a){
		this.authenticator=a;
	}
	/**
	 * @return the maxCommandHistory
	 */
	public int getMaxCommandHistory() {
		return maxCommandHistory;
	}
	/**
	 * @param maxCommandHistory the maxCommandHistory to set
	 */
	public void setMaxCommandHistory(int maxCommandHistory) {
		this.maxCommandHistory = maxCommandHistory;
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
		saveCommandHistory();
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
