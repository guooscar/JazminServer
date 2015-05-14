package jazmin.driver.memcached;
import jazmin.core.Jazmin;
import jazmin.server.console.ascii.FormPrinter;
import jazmin.server.console.ascii.TablePrinter;
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
    	addOption("set_poolsize",true,"set connection pool size",this::setPoolSize);
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
    	FormPrinter tp=FormPrinter.create(out,20);
    	tp.print("name",driver.getName());
    	tp.print("servers",driver.getServerAddrs());
    	tp.print("idleTimeout",driver.getOpTimeout());
    	tp.print("connectTimeout",driver.getConnectTimeout());
    	tp.print("connectionPoolSize",driver.getConnectionPoolSize());
	}
    //
    private void setPoolSize(String args){
    	driver.setConnectionPoolSize(Integer.valueOf(args));
    	out.println("connection pool size set to "+args);
    }
    //
    private void showQueryStat(String args){
    	TablePrinter tp=TablePrinter.create(out)
    			.length(20,20,20,20)
    			.headers("QUERY","HIT","ERROR","HIT RATIO");
    	double qc=driver.getTotalQueryCount();
    	double hit=driver.getHitQueryCount();
    	double hitRatio=(driver.getTotalQueryCount()==0)?0:hit/qc;
    	tp.print(
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
