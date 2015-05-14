package jazmin.driver.memcached;


/**
 * 
 * @author yama
 *
 */
public class SmartPrefixSafeCache<T> extends PrefixSafeCache{
	//
	public SmartPrefixSafeCache() {
		super();
	}
	/**
	 * load cache object with key
	 */
	@SuppressWarnings("unchecked")
	public T get(String key){
		return (T) super.get(key);
	}
	
	/**
	 * put cache object to memcached
	 */
	@SuppressWarnings("unchecked")
	public void set(String key,Object v){
		T vv=(T) v;
		super.set( key, vv);
	}
	
}
