package jazmin.driver.memcached;
import jazmin.core.Jazmin;
import jazmin.server.console.builtin.ConsoleCommand;
import jazmin.util.DumpUtil;
/**
 * 
 * @author yama
 * 26 Dec, 2014
 */
public class MemcachedDriverCommand extends ConsoleCommand {
    private MemcachedDriver driver;
	public MemcachedDriverCommand() {
    	super();
    	id="memcached";
    	desc="memcached driver ctrl command";
    	addOption("i",false,"show server information.",this::showDriverInfo);
    	addOption("stat",false,"show query stat.",this::showQueryStat);
    	addOption("get",true,"get memory cache item by key",this::getItem);
    	addOption("delete",true,"delete memory cache item by key",this::deleteItem);
    	//
    	driver=Jazmin.getDriver(MemcachedDriver.class);
    }
	//
	@Override
	public void run() throws Exception{
		 if(driver==null){
			 err.println("can not find MemcachedDriver.");
			 return;
		 }
		 super.run();
	}
    //
    private void showDriverInfo(String args){
    	String format="%-20s: %-10s\n";
    	out.printf(format,"name",driver.getName());
    	out.printf(format,"port",driver.getServerAddr());
		out.printf(format,"idleTimeout",driver.getOpTimeout());
		out.printf(format,"connectTimeout",driver.getConnectTimeout());
		out.printf(format,"connectionPoolSize",driver.getConnectionPoolSize());
	}
    //
    private void showQueryStat(String args){
    	String format="%-20s %-20s %-20s %-20s\n";
    	out.printf(format,"QUERY","HIT","ERROR","HIT RATIO");
    	double qc=driver.getTotalQueryCount();
    	double hit=driver.getHitQueryCount();
    	double hitRatio=(driver.getTotalQueryCount()==0)?0:hit/qc;
    	out.printf(format,
    			driver.getTotalQueryCount(),
    			driver.getHitQueryCount(),
    			driver.getErrorQueryCount(),
    			String.format("%.2f",hitRatio));
    	
    }
    //
    private void getItem(String args)throws Exception{
    	Object ret=null;
    	if(args!=null){
    		ret=driver.get(args);
    	}
    	if(ret==null){
    		out.println("can not find item with key:"+args);
    		return;
    	}
    	out.println(DumpUtil.dump(ret));
    }
    //
    private void deleteItem(String args)throws Exception{
    	if(args!=null){
    		boolean dr=driver.delete(args);
    		out.println("delete :"+args+" ret:"+dr);
    	}
    }
}
