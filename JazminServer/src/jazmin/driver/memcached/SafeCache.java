package jazmin.driver.memcached;

import jazmin.core.app.AutoWired;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;

/**
 * 
 * @author yama
 *
 */
public class SafeCache {
	private static  Logger logger=LoggerFactory.get(SafeCache.class);
	
	@AutoWired
	protected MemcachedDriver cacheResource;
	private int expireTime;
	//
	public SafeCache() {
		this.expireTime=180;
	}
	//
	public SafeCache(int expireTime) {
		this.expireTime=expireTime;
	}
	public SafeCache(int expireTime,MemcachedDriver driver) {
		this.expireTime=expireTime;
		setCacheResource(driver);
	}
	/**
	 * @return the expireTime
	 */
	public int getExpireTime() {
		return expireTime;
	}

	/**
	 * @param expireTime the expireTime to set
	 */
	public void setExpireTime(int expireTime) {
		this.expireTime = expireTime;
	}

	/**
	 * @return the cacheResource
	 */
	public MemcachedDriver getCacheResource() {
		return cacheResource;
	}

	/**
	 * @param cacheResource the cacheResource to set
	 */
	public void setCacheResource(MemcachedDriver cacheResource) {
		this.cacheResource = cacheResource;
	}
	
	/**
	 * load cache object with key
	 */
	public Object get(String key){
		try {
			return cacheResource.get(key);
		} catch (MemcachedException e) {
			logger.error(e.getMessage(),e);
			return null;
		}
	}
	
	/**
	 * put cache object to memcached
	 */
	public void set(String key,Object v){
		if(v==null){
			return;
		}
		try {
			cacheResource.set(key, expireTime, v);
		} catch (MemcachedException e) {
			logger.error(e.getMessage(),e);
		}
	}
	
	/**
	 * remove cache object
	 */
	public void delete(String key){
		try {
			cacheResource.delete(key);
		} catch (MemcachedException e) {
			logger.error(e.getMessage(),e);
		}
	}
}
