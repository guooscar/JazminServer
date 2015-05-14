package jazmin.driver.memcached;


/**
 * 
 * @author yama
 *
 */
public class PrefixSafeCache extends SafeCache{
	private String prefix="";
	
	public PrefixSafeCache() {
		super();
	}
	
	/**
	 * @return the prefix
	 */
	public String getPrefix() {
		return prefix;
	}


	/**
	 * @param prefix the prefix to set
	 */
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	

	/**
	 * load cache object with key
	 */
	public Object get(String key){
		return super.get(prefix+key);
	}
	
	/**
	 * put cache object to memcached
	 */
	public void set(String key,Object v){
		super.set(prefix+key, v);
	}
	
	/**
	 * remove cache object
	 */
	public void delete(String key){
		super.delete(prefix+key);
	}
}
