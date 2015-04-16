package jazmin.driver.redis;
import jazmin.core.Jazmin;
import jazmin.server.console.ConsoleCommand;
import jazmin.util.DumpUtil;
import redis.clients.jedis.JedisShardInfo;
/**
 * 
 * @author yama
 * 26 Dec, 2014
 */
public class RedisDriverCommand extends ConsoleCommand {
    private RedisDriver driver;
	public RedisDriverCommand() {
    	super();
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
			 err.println("can not find RedisDriver.");
			 return;
		 }
		 super.run();
	}
    //
    private void showDriverInfo(String args){
    	String format="%-20s: %-10s\n";
		out.format(format, "maxTotal", driver.getMaxTotal());
		out.format(format, "maxIdle", driver.getMaxIdle());
		out.format(format, "minIdle", driver.getMinIdle());
		out.format(format, "maxWaitMillis", driver.getMaxWaitMillis());
		out.format(format, "testOnBorrow", driver.getTestOnBorrow());
		out.format(format, "testOnCreate", driver.getTestOnCreate());
		out.format(format, "testOnReturn", driver.getTestOnReturn());
		out.format(format, "testWhileIdle", driver.getTestWhileIdle());
		//
		int index=0;
		for(JedisShardInfo s:driver.shardInfos){
			out.format(format,"shard-"+(index++),s.getHost()+":"+s.getPort()+"/"+s.getName());						
		}	
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
