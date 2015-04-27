package jazmin.server.ftp;
import java.util.List;
import java.util.concurrent.TimeUnit;

import jazmin.core.Jazmin;
import jazmin.server.console.ConsoleCommand;
import jazmin.server.console.TerminalWriter;
/**
 * 
 * @author yama
 * 26 Dec, 2014
 */
public class FTPServerCommand extends ConsoleCommand {
    private FTPServer ftpServer;
	public FTPServerCommand() {
    	super();
    	id="ftpsrv";
    	desc="ftp server ctrl command";
    	addOption("i",false,"show server information.",this::showServerInfo);
    	addOption("session",false,"show sessions.",this::showSessions);
    	addOption("stat",false,"show stat info.",this::showStats);
    	addOption("statop",false,"show stat info.",this::showStatsTop);
    	addOption("list",false,"list upload/download files.",this::showList);
    	//
    	ftpServer=Jazmin.getServer(FTPServer.class);
    }
	//
	@Override
	public void run() throws Exception{
		 if(ftpServer==null){
			 err.println("can not find FTPServer.");
			 
			 return;
		 }
		 super.run();
	}
	//
	 private void showStats(String args){
	    	String format="%-30s : %-50s\n";
			FTPStatistics stat=ftpServer.getStatistics();
			out.format(format,"#","VALUE");	
			out.format(format,"startTime",stat.getStartTime());
			out.format(format,"currentAnonymousLoginNumber",stat.getCurrentAnonymousLoginNumber());
			out.format(format,"currentConnectionNumber",stat.getCurrentConnectionNumber());
			out.format(format,"currentLoginNumber",stat.getCurrentLoginNumber());
			out.format(format,"totalAnonymousLoginNumber",stat.getTotalAnonymousLoginNumber());
			out.format(format,"totalConnectionNumber",stat.getTotalConnectionNumber());
			out.format(format,"totalDeleteNumber",stat.getTotalDeleteNumber());
			out.format(format,"totalDirectoryCreated",stat.getTotalDirectoryCreated());
			out.format(format,"totalDirectoryRemoved",stat.getTotalDirectoryRemoved());
			out.format(format,"totalDownloadSize",stat.getTotalDownloadSize());
			out.format(format,"totalDownloadNumber",stat.getTotalDownloadNumber());
			out.format(format,"totalFailedLoginNumber",stat.getTotalFailedLoginNumber());
			out.format(format,"totalLoginNumber",stat.getTotalLoginNumber());
			out.format(format,"totalUploadNumber",stat.getTotalUploadNumber());
			out.format(format,"totalUploadSize",stat.getTotalUploadSize());
	    }
	    //
	    private void showStatsTop(String args)throws Exception{
	    	TerminalWriter tw=new TerminalWriter(out);
	    	while(stdin.available()==0){
	    		tw.cls();
	    		out.println("press any key to quit.");
	    		showStats(args);
	    		out.flush();
	    		TimeUnit.SECONDS.sleep(1);
	    	}
	    	stdin.read();
	    }
    //
    private void showServerInfo(String args){
    	String format="%-25s: %-10s\n";
		out.printf(format,"port",ftpServer.getPort());
		out.printf(format,"idleTimeout",ftpServer.getIdleTimeout());
		out.printf(format,"implicitSsl",ftpServer.isImplicitSsl());
		out.printf(format,"serverAddress",ftpServer.getServerAddress());
		out.printf(format,"port",ftpServer.getPort());
		out.printf(format,"loginFailureDelay",ftpServer.getLoginFailureDelay());
		out.printf(format,"maxAnonymousLogins",ftpServer.getMaxAnonymousLogins());
		out.printf(format,"maxLoginFailures", ftpServer.getMaxLoginFailures());
		out.printf(format,"maxLogins", ftpServer.getMaxLogins());
		out.printf(format,"maxThreads", ftpServer.getMaxThreads());
		out.printf(format,"anonymousLoginEnabled",ftpServer.isAnonymousLoginEnabled());
		out.printf(format,"activeLocalAddress",ftpServer.getActiveLocalAddress());
		out.printf(format,"activeLocalPort",ftpServer.getActiveLocalPort());
		out.printf(format,"idleTime",ftpServer.getIdleTime());
		out.printf(format,"passiveAddress",ftpServer.getPassiveAddress());
		out.printf(format,"passiveExternalAddress",ftpServer.getPassiveExternalAddress());
		out.printf(format,"passivePorts",ftpServer.getPassivePorts());
		out.printf(format,"activeEnabled",ftpServer.isActiveEnabled());
		out.printf(format,"activeIpCheck",ftpServer.isActiveIpCheck());
		out.printf(format,"commandListener", ftpServer.getCommandListener());
		out.printf(format,"userManager", ftpServer.getUserManager());
    }
    //
    private void showList(String args){
    	List<FileTransferInfo>transferList=ftpServer.getFileTransferInfos();
    	out.format("total %d transfers\n",transferList.size());
    	String format="%-5s : %-6s %-20s %-20s %-30s\n";
		int i=0;
		out.format(format,"#","TYPE","SESSION","STARTTIME","FILE");	
		for(FileTransferInfo s:transferList){
			out.format(format,
					i++,
					s.type,
					s.session.getUser().userName+"@"+
					s.session.getClientAddress().getAddress().getHostAddress()
					+":"+
					s.session.getClientAddress().getPort(),
					formatDate(s.startTime),
					s.file);
		};
    }
    //
    private void showSessions(String args){
    	List<FTPSession>sessions=ftpServer.getSessions();
    	out.format("total %d sessions\n",sessions.size());
    	String format="%-5s : %-10s %-20s %-10s %-10s %-15s %-15s %-15s %-10s %-10s %-10s\n";
		int i=0;
		out.format(format,
				"#",
				"USER",
				"ADDR",
				"FLOGINS",
				"FOFFSET",
				"CONNTIME",
				"LASTACCTIME",
				"LOGINTIME",
				"MAXIDLE",
				"ISLOGIN",
				"ISSECURE");	
		for(FTPSession s:sessions){
			out.format(format,
					i++,
					s.getUser().userName,
					s.getClientAddress().getAddress().getHostAddress()+":"+s.getClientAddress().getPort(),
					s.getFailedLogins(),
					s.getFileOffset(),
					formatDate(s.getConnectionTime()),
					formatDate(s.getLastAccessTime()),
					formatDate(s.getLoginTime()),
					s.getMaxIdleTime(),
					s.isLoggedIn(),
					s.isSecure());
		};
    }
}
