package jazmin.driver.jdbc;
import java.util.Collections;
import java.util.List;

import jazmin.core.Jazmin;
import jazmin.misc.InvokeStat;
import jazmin.server.console.ConsoleCommand;
/**
 * 
 * @author yama
 * 26 Dec, 2014
 */
public class C3P0DriverCommand extends ConsoleCommand {
    private C3P0ConnectionDriver connectionDriver;
	public C3P0DriverCommand() {
    	super();
    	id="c3p0";
    	desc="cp30 connection driver ctrl command";
    	addOption("i",false,"show driver information.",this::showDriverInfo);
    	addOption("enablestat",false,"enable stat sql.",this::enableStatSql);
    	addOption("disablestat",false,"disable stat sql.",this::disableStatSql);
    	addOption("stat",false,"show sql stat.",this::showStats);
    	addOption("f",false,"show full sql",null);
    	
    	//
    	connectionDriver=Jazmin.driver(C3P0ConnectionDriver.class);
    }
	//
	@Override
	public void run() throws Exception{
		 if(connectionDriver==null){
			 err.println("can not find C3P0ConnectionDriver.");
			 return;
		 }
		 super.run();
	}
	//
	  //
    private void showStats(String args){
    	String format="%-5s : %-10s %-10s %-10s %-10s %-10s %-50s \n";
		int i=0;
		List<InvokeStat>stats=connectionDriver.invokeStats();
		out.println("total "+stats.size()+" sql stats");
		Collections.sort(stats);
		out.format(format,"#","IVC","ERR","MINT","MAXT","AVGT","SQL");	
		for(InvokeStat stat:stats){
			String sql=stat.name;
			if(!cli.hasOption('f')){
				if(sql.length()>=81){
					sql=sql.substring(0,80);
				}
			}
			out.format(format,i++,
					stat.invokeCount,
					stat.errorCount,
					stat.minTime,
					stat.maxTime,
					stat.avgTime(),
					sql);
		};
    }
	//
	private void enableStatSql(String args)throws Exception{
		connectionDriver.setStatSql(true);
	}
	//
	//
	private void disableStatSql(String args)throws Exception{
		connectionDriver.setStatSql(false);
	}
    //
    private void showDriverInfo(String args)throws Exception{
    	String format="%-20s: %-10s\n";
		out.printf(format,"url",connectionDriver.url());
		out.printf(format,"user",connectionDriver.user());
		out.printf(format,"autoCommitOnClose",connectionDriver.autoCommitOnClose());
		out.printf(format,"checkoutTimeout",connectionDriver.checkoutTimeout());
		out.printf(format,"driverClass",connectionDriver.driverClass());
		out.printf(format,"initialPoolSize",connectionDriver.initialPoolSize());
		out.printf(format,"loginTimeout",connectionDriver.loginTimeout());
		out.printf(format,"maxConnectionAge",connectionDriver.maxConnectionAge());
		out.printf(format,"minPoolSize",connectionDriver.minPoolSize());
		out.printf(format,"maxPoolSize",connectionDriver.maxPoolSize());
		out.printf(format,"threadPoolSize",connectionDriver.threadPoolSize());
		//
		out.printf(format,"numConnections",connectionDriver.numConnections());
		out.printf(format,"numIdleConnections",connectionDriver.numIdleConnections());
		out.printf(format,"threadPoolNumActiveThreads",connectionDriver.threadPoolNumActiveThreads());
		out.printf(format,"threadPoolNumIdleThreads",connectionDriver.threadPoolNumIdleThreads());
		out.printf(format,"threadPoolNumTasksPending",connectionDriver.threadPoolNumTasksPending());
		//
		out.printf(format,"statSql",connectionDriver.isStatSql());
		
    }
    
}
