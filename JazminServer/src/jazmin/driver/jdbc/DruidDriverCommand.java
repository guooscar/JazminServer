package jazmin.driver.jdbc;
import java.util.Collections;
import java.util.List;

import jazmin.core.Jazmin;
import jazmin.misc.io.InvokeStat;
import jazmin.server.console.builtin.ConsoleCommand;

/**
 * 
 * @author skydu
 *
 */
public class DruidDriverCommand extends ConsoleCommand {
    private DruidConnectionDriver connectionDriver;
	public DruidDriverCommand() {
    	super(true);
    	id="druid";
    	desc="druid connection driver ctrl command";
    	addOption("i",false,"show driver information.",this::showDriverInfo);
    	addOption("enablestat",false,"enable stat sql.",this::enableStatSql);
    	addOption("disablestat",false,"disable stat sql.",this::disableStatSql);
    	addOption("stat",false,"show sql stat.",this::showStats);
    	addOption("f",false,"show full sql",null);
    	addOption("monitor",false,"show monitor information.",this::showMonitorInfo);
    	
    	//
    	connectionDriver=Jazmin.getDriver(DruidConnectionDriver.class);
    }
	//
	@Override
	public void run() throws Exception{
		 if(connectionDriver==null){
			 out.println("can not find DruidConnectionDriver.");
			 return;
		 }
		 super.run();
	}
	//
    private void showStats(String args){
    	String format="%-5s : %-10s %-10s %-10s %-10s %-10s %-50s \n";
		int i=1;
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
					stat.minFullTime,
					stat.maxFullTime,
					stat.avgFullTime(),
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
    	out.println(connectionDriver.info());
    }
    //
    private void showMonitorInfo(String args)throws Exception{
    	String format="%-10s : %-10s\n";
		out.format(format,"MaxActive",connectionDriver.getMaxActive());
    	out.format(format,"MinIdle",connectionDriver.getMinIdle());
    	out.format(format,"MaxWait",connectionDriver.getMaxWait());
    	out.format(format,"ActiveCount",connectionDriver.getActiveCount());
    	out.format(format,"ActivePeak",connectionDriver.getActivePeak());  	
    }
    
}
