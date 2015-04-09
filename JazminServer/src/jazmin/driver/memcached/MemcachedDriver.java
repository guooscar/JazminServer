package jazmin.driver.memcached;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;

import jazmin.core.Driver;
import jazmin.core.Jazmin;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.misc.InfoBuilder;
import jazmin.server.console.ConsoleServer;
import jazmin.util.DumpUtil;
import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.MemcachedClientBuilder;
import net.rubyeye.xmemcached.XMemcachedClientBuilder;
import net.rubyeye.xmemcached.impl.KetamaMemcachedSessionLocator;
import net.rubyeye.xmemcached.transcoders.SerializingTranscoder;
import net.rubyeye.xmemcached.utils.AddrUtil;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
/**
 * 
 * @author yama
 * 27 Dec, 2014
 */
public class MemcachedDriver extends Driver{
	private static Logger logger=LoggerFactory.get(MemcachedDriver.class);
	//--------------------------------------------------------------------------
	private MemcachedClient memcachedClient;
	private String serverAddr;
	private int connectionPoolSize=4;
	private int opTimeout=5000;
	private LongAdder totalQueryCount;
	private LongAdder hitQueryCount;
	private LongAdder errorQueryCount;
	public MemcachedDriver() {
		totalQueryCount=new LongAdder();
		hitQueryCount=new LongAdder();
		errorQueryCount=new LongAdder();	
	}
	
	//--------------------------------------------------------------------------
	static class JSONSerializingTranscoder extends SerializingTranscoder{
		@Override
		protected Object deserialize(byte[] in) {
			return JSON.parse(in);
		}
		//
		@Override
		protected byte[] serialize(Object obj) {
			return JSON.toJSONBytes(obj,SerializerFeature.WriteClassName);
		}
	}

	//--------------------------------------------------------------------------
	/**
	 * @return
	 * @see net.rubyeye.xmemcached.MemcachedClient#getConnectTimeout()
	 */
	public long getConnectTimeout() {
		return memcachedClient.getConnectTimeout();
	}
	/**
	 * @return
	 * @see net.rubyeye.xmemcached.MemcachedClient#getHealSessionInterval()
	 */
	public long getHealSessionInterval() {
		return memcachedClient.getHealSessionInterval();
	}
	/**
	 * @return
	 * @see net.rubyeye.xmemcached.MemcachedClient#getName()
	 */
	public String getName() {
		return memcachedClient.getName();
	}
	/**
	 * @return
	 * @see net.rubyeye.xmemcached.MemcachedClient#getOpTimeout()
	 */
	public long getOpTimeout() {
		return memcachedClient.getOpTimeout();
	}
	
	/**
	*/
	public Map<InetSocketAddress, Map<String, String>> getStats()
			throws MemcachedException {
		try{
			return memcachedClient.getStats();
		}catch(Exception e){
			throw new MemcachedException(e);
		}
	}
	
	/**
	 */
	public void flushAll() throws MemcachedException {
		try{
			memcachedClient.flushAll();
		}catch(Exception e){
			throw new MemcachedException(e);
		}
	}
	
	/**
	 */
	public void flushAll(InetSocketAddress arg0) throws MemcachedException  {
		try{
			memcachedClient.flushAll(arg0);
		}catch(Exception e){
			throw new MemcachedException(e);
		}
	}
	/**
	 */
	public Map<InetSocketAddress, String> getVersions()
			throws MemcachedException  {
		try{
			return memcachedClient.getVersions();
		}catch(Exception e){
			throw new MemcachedException(e);
		}
	}
	//
	
	/**
	 * @return String
	 */
	public String getServerAddr() {
		return serverAddr;
	}
	
	public void setOpTimeout(long arg0) {
		if(inited()){
			throw new IllegalArgumentException("set before inited");
		}
		memcachedClient.setOpTimeout(arg0);
	}
	/**
	 */
	public void setServerAddr(String serverAddr) {
		if(inited()){
			throw new IllegalArgumentException("set before inited");
		}
		this.serverAddr = serverAddr;
	}
	/**
	 * 
	 * @return
	 */
	public int getConnectionPoolSize() {
		return connectionPoolSize;
	}
	/**
	 * 
	 * @param connectionPoolSize
	 */
	public void setConnectionPoolSize(int connectionPoolSize) {
		this.connectionPoolSize = connectionPoolSize;
	}
	//life cycle
	//--------------------------------------------------------------------------
	//public 
	/**
	 */
	public boolean add(String key,int expireTime,Object obj) 
	throws MemcachedException{
		try {
			return memcachedClient.add(key, expireTime,obj);
		} catch (Exception e) {
			throw new MemcachedException(e);
		} 
	}
	/**
	 */
	public boolean set(String key,int expireTime,Object obj) 
	throws MemcachedException{
		try {
			if(logger.isDebugEnabled()){
				logger.debug("set key {}/{}-\n{}",key,expireTime,DumpUtil.dump(obj));
			}
			return memcachedClient.set(key, expireTime,obj);
		} catch (Exception e) {
			throw new MemcachedException(e);
		} 
	}
	//
	/**
	 */
	public Object get(String key) 
	throws MemcachedException{
		totalQueryCount.increment();
		try {
			Object o= memcachedClient.get(key);
			if(o!=null){
				hitQueryCount.increment();
			}
			if(logger.isDebugEnabled()){
				logger.debug("get key {}-\n{}",key,DumpUtil.dump(o));
			}
			return o;
		} catch (Exception e) {
			errorQueryCount.increment();
			throw new MemcachedException(e);
		} 
	}
	//
	/**
	 */
	public boolean replace(String key,int expireTime,Object obj) 
	throws MemcachedException{
		try {
			if(logger.isDebugEnabled()){
				logger.debug("replace key {}/{}-\n{}",key,expireTime,DumpUtil.dump(obj));
			}
			return memcachedClient.replace(key, expireTime, obj);
		} catch (Exception e) {
			throw new MemcachedException(e);
		} 
	}
	/**
	 */
	public boolean delete(String key) throws MemcachedException{
		try {
			boolean b= memcachedClient.delete(key);
			if(logger.isDebugEnabled()){
				logger.debug("delete key {}/{}",key,b);
			}
			return b;
		} catch (Exception e) {
			throw new MemcachedException(e);
		} 
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
	/**
	 * 
	 */
	public long getErrorQueryCount() {
		return errorQueryCount.longValue();
	}
	//--------------------------------------------------------------------------
	/**
	 */
	@Override
	public void init() throws Exception{
		if(serverAddr==null){
			logger.warn("can not find server addr");
			return;
		}
		MemcachedClientBuilder builder=new XMemcachedClientBuilder(
				AddrUtil.getAddresses(serverAddr));
		builder.setSessionLocator(new KetamaMemcachedSessionLocator());
		builder.setConnectionPoolSize(connectionPoolSize);
		memcachedClient=builder.build();
		memcachedClient.setOpTimeout(opTimeout);
		ConsoleServer cs=Jazmin.getServer(ConsoleServer.class);
		if(cs!=null){
			cs.registerCommand(new MemcachedDriverCommand());
		}
	}
	//
	@Override
	public void start() {
		JSONSerializingTranscoder transCoder=new JSONSerializingTranscoder();
		memcachedClient.setTranscoder(transCoder);		
	}
	@Override
	public void stop() {
		if(memcachedClient!=null){
			try {
				memcachedClient.shutdown();
			} catch (IOException e) {
				logger.error(e.getMessage(),e);
			}
		}
	}
	//
	@Override
	public String info() {
		return InfoBuilder.create().format("%-30s:%-30s\n")
		.print("serverAddr",getServerAddr())
		.print("connectionPoolSize",getConnectionPoolSize())
		.print("name",getName())
		.print("connectTimeout",getConnectTimeout())
		.print("opTimeout",getOpTimeout())
		.toString();
	}
}
