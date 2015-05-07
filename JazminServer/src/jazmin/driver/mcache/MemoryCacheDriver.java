/**
 * 
 */
package jazmin.driver.mcache;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

import jazmin.core.Driver;
import jazmin.core.Jazmin;
import jazmin.misc.InfoBuilder;
import jazmin.server.console.ConsoleServer;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;

/**
 * @author yama
 * 30 Jan, 2015
 */
public class MemoryCacheDriver extends Driver{
	private int maxCacheCount=10000;
	private ConcurrentLinkedHashMap<String,MemoryCacheItem>itemMap;
	private LongAdder totalQueryCount;
	private LongAdder hitQueryCount;
	//
	public MemoryCacheDriver() {
		totalQueryCount=new LongAdder();
		hitQueryCount=new LongAdder();
	}
	/**
	 * @return the totalQueryCount
	 */
	public long getTotalQueryCount() {
		return totalQueryCount.longValue();
	}
	/**
	 * @return the hitQueryCount
	 */
	public long getHitQueryCount() {
		return hitQueryCount.longValue();
	}
	//
	public int getCacheSize(){
		return itemMap.size();
	}
	//
	public boolean delete(String key){
		MemoryCacheItem item=itemMap.remove(key);
		return item!=null;
	}
	//
	public Object get(String key){
		totalQueryCount.increment();
		MemoryCacheItem item=itemMap.get(key);
		if(item==null){
			return null;
		}
		hitQueryCount.increment();
		return item.data;
	}
	//
	public void add(String key,Object data,int ttlInMinutes){
		MemoryCacheItem item=new MemoryCacheItem();
		item.createTime=System.currentTimeMillis();
		item.data=data;
		item.ttlInMinutes=ttlInMinutes;
		itemMap.put(key,item);
	}
	//
	private void checkCacheItem(){
		long currTime=System.currentTimeMillis();
		for(MemoryCacheItem item:itemMap.values()){
			if((currTime-item.createTime)>item.ttlInMinutes*60*1000L){
				itemMap.remove(item.id);
			}
		}
	}
	
	/**
	 * @return the maxCacheCount
	 */
	public int getMaxCacheCount() {
		return maxCacheCount;
	}
	/**
	 * @param maxCacheCount the maxCacheCount to set
	 */
	public void setMaxCacheCount(int maxCacheCount) {
		this.maxCacheCount = maxCacheCount;
	}
	//--------------------------------------------------------------------------
	@Override
	public void init() throws Exception {
		itemMap =new ConcurrentLinkedHashMap.Builder<String,MemoryCacheItem>()
			    .maximumWeightedCapacity(maxCacheCount)
			    .build();
		
		ConsoleServer cs=Jazmin.getServer(ConsoleServer.class);
		if(cs!=null){
			cs.registerCommand(MemoryCacheDriverCommand.class);
		}
	}
	//
	@Override
	public void start() throws Exception {
		Jazmin.scheduleAtFixedRate(this::checkCacheItem, 0, 1, TimeUnit.MINUTES);
	}
	//
	@Override
	public String info() {
		InfoBuilder ib=new InfoBuilder();
		ib.format("%-30s : %-30s\n");
		ib.print("maxCacheCount",maxCacheCount);
		return ib.toString();
	}
}
