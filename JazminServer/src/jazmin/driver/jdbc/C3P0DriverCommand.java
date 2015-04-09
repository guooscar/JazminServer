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
    	connectionDriver=Jazmin.getDriver(C3P0ConnectionDriver.class);
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
		List<InvokeStat>stats=connectionDriver.getInvokeStats();
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
		out.printf(format,"url",connectionDriver.getUrl());
		out.printf(format,"user",connectionDriver.getUser());
		out.printf(format,"autoCommitOnClose",connectionDriver.isAutoCommitOnClose());
		out.printf(format,"checkoutTimeout",connectionDriver.getCheckoutTimeout());
		out.printf(format,"driverClass",connectionDriver.getDriverClass());
		out.printf(format,"initialPoolSize",connectionDriver.getInitialPoolSize());
		out.printf(format,"loginTimeout",connectionDriver.getLoginTimeout());
		out.printf(format,"maxConnectionAge",connectionDriver.getMaxConnectionAge());
		out.printf(format,"minPoolSize",connectionDriver.getMinPoolSize());
		out.printf(format,"maxPoolSize",connectionDriver.getMaxPoolSize());
		out.printf(format,"threadPoolSize",connectionDriver.getThreadPoolSize());
		//
		out.printf(format,"numConnections",connectionDriver.getNumConnections());
		out.printf(format,"numIdleConnections",connectionDriver.getNumIdleConnections());
		out.printf(format,"threadPoolNumActiveThreads",connectionDriver.getThreadPoolNumActiveThreads());
		out.printf(format,"threadPoolNumIdleThreads",connectionDriver.getThreadPoolNumIdleThreads());
		out.printf(format,"threadPoolNumTasksPending",connectionDriver.getThreadPoolNumTasksPending());
		//
		out.printf(format,"statSql",connectionDriver.isStatSql());
		
    }
    
}
