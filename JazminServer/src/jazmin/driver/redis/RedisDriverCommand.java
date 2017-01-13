package jazmin.driver.redis;
import jazmin.core.Jazmin;
import jazmin.server.console.builtin.ConsoleCommand;
import jazmin.util.DumpUtil;
/**
 * 
 * @author yama
 * 26 Dec, 2014
 */
public class RedisDriverCommand extends ConsoleCommand {
    private RedisDriver driver;
	public RedisDriverCommand() {
    	super(true);
    	id="redis";
    	desc="redis driver ctrl command";
    	addOption("i",false,"show server information.",this::showDriverInfo);
    	addOption("get",true,"get  item by key",this::getItem);
    	addOption("delete",true,"delete item by key",this::deleteItem);
    	//
    	driver=Jazmin.getDriver(RedisDriver.class);
    }
	//
	@Override
	public void run() throws Exception{
		 if(driver==null){
			 out.println("can not find RedisDriver.");
			 return;
		 }
		 super.run();
	}
    //
    private void showDriverInfo(String args){
    	out.println(driver.info());
	}
    //
    //
    private void getItem(String args)throws Exception{
    	Object ret=null;
    	if(args!=null){
    		RedisConnection conn=driver.getConnection();
    		ret=conn.get(args);
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
    		RedisConnection conn=driver.getConnection();
    		Long l=conn.del(args);
    		out.println("delete :"+args+" ret:"+l);
    	}
    }
   
}
