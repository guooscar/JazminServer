package jazmin.driver.jdbc;
import java.util.Collections;
import java.util.List;

import jazmin.core.Jazmin;
import jazmin.misc.io.InvokeStat;
import jazmin.server.console.builtin.ConsoleCommand;
/**
 * 
 * @author yama
 * 26 Dec, 2014
 */
public class C3p0DriverCommand extends ConsoleCommand {
    private C3p0ConnectionDriver connectionDriver;
	public C3p0DriverCommand() {
    	super();
    	id="c3p0";
    	desc="cp30 connection driver ctrl command";
    	addOption("i",false,"show driver information.",this::showDriverInfo);
    	addOption("enablestat",false,"enable stat sql.",this::enableStatSql);
    	addOption("disablestat",false,"disable stat sql.",this::disableStatSql);
    	addOption("stat",false,"show sql stat.",this::showStats);
    	addOption("max_poolsize",true,"set max pool size.",this::setMaxPoolSize);
    	addOption("f",false,"show full sql",null);
    	
    	//
    	connectionDriver=Jazmin.getDriver(C3p0ConnectionDriver.class);
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
	private void setMaxPoolSize(String args){
		connectionDriver.setMaxPoolSize(Integer.valueOf(args));
		out.println("max pool size set to :"+args);
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
    
}
