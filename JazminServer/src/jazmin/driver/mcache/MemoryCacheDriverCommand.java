package jazmin.driver.mcache;
import jazmin.core.Jazmin;
import jazmin.server.console.ConsoleCommand;
import jazmin.util.DumpUtil;
/**
 * 
 * @author yama
 * 26 Dec, 2014
 */
public class MemoryCacheDriverCommand extends ConsoleCommand {
    private MemoryCacheDriver driver;
	public MemoryCacheDriverCommand() {
    	super();
    	id="mcache";
    	desc="mcache driver ctrl command";
    	addOption("i",false,"show server information.",this::showDriverInfo);
    	addOption("stat",false,"show query stat.",this::showQueryStat);
    	addOption("get",true,"get memory cache item by key",this::getItem);
    	addOption("delete",true,"delete memory cache item by key",this::deleteItem);
    	//
    	driver=Jazmin.getDriver(MemoryCacheDriver.class);
    }
	//
	@Override
	public void run() throws Exception{
		 if(driver==null){
			 err.println("can not find MemoryCacheDriver.");
			 return;
		 }
		 super.run();
	}
    //
    private void showDriverInfo(String args){
    	String format="%-20s: %-10s\n";
    	out.printf(format,"maxCacheCount",driver.getMaxCacheCount());
    	out.printf(format,"size",driver.getCacheSize());
    }
    //
    private void showQueryStat(String args){
    	String format="%-20s %-20s %-20s\n";
    	out.printf(format,"QUERY","HIT","HIT RATIO");
    	double qc=driver.getTotalQueryCount();
    	double hit=driver.getHitQueryCount();
    	double hitRatio=(driver.getTotalQueryCount()==0)?0:hit/qc;
    	out.printf(format,
    			driver.getTotalQueryCount(),
    			driver.getHitQueryCount(),
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
