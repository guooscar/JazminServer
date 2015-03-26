package jazmin.server.ftp;
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
    	addOption("u",false,"show all user names.",this::showUsers);
    	addOption("stat",false,"show stat info.",this::showStats);
    	addOption("statop",false,"show stat info.",this::showStatsTop);
    	
    	//
    	ftpServer=Jazmin.server(FTPServer.class);
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
    	String format="%-20s: %-10s\n";
		out.printf(format,"port",ftpServer.getPort());
		out.printf(format,"idleTimeout",ftpServer.getIdleTimeout());
		out.printf(format,"implicitSsl",ftpServer.isImplicitSsl());
		out.printf(format,"serverAddress",ftpServer.getServerAddress());
		out.printf(format,"adminUser",ftpServer.getAdminUser());
		out.printf(format,"port",ftpServer.getPort());
		out.printf(format,"commandListener",ftpServer.getCommandListener());
	}
    //
    private void showUsers(String args){
    	String users[]=ftpServer.getAllUserNames();
    	out.format("total %d users\n",users.length);
    	String format="%-5s : %-30s \n";
		int i=0;
		out.format(format,"#","NAME");	
		for(String s:users){
			out.format(format,
					i++,
					s);
		};
    }
}
