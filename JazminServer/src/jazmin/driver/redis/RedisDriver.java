/**
 * 
 */
package jazmin.driver.redis;

import java.util.ArrayList;
import java.util.List;

import jazmin.core.Driver;
import jazmin.core.Jazmin;
import jazmin.misc.InfoBuilder;
import jazmin.server.console.ConsoleServer;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedisPool;
import redis.clients.util.Hashing;

/**
 *
 */
public class RedisDriver extends Driver{
	ShardedJedisPool pool;
	JedisPoolConfig config;
	List<JedisShardInfo>shardInfos;
	public RedisDriver() {
		config=new JedisPoolConfig();
		shardInfos=new ArrayList<JedisShardInfo>();
		config.setMaxIdle(200);
		config.setMaxTotal(1024);
		config.setTestOnBorrow(true);
		config.setTestOnReturn(true);
	}
	//
	public void addShard(String host,int port){
		shardInfos.add(new JedisShardInfo(host, port));
	}
	//
	public void addShard(String host,int port,String name){
		shardInfos.add(new JedisShardInfo(host, port,name));
	}
	//
	public void addShard(String host,int port,int timeout){
		shardInfos.add(new JedisShardInfo(host, port,timeout));
	}
	//
	/**
	 * @return
	 * @see org.apache.commons.pool2.impl.GenericObjectPoolConfig#getMaxIdle()
	 */
	public int getMaxIdle() {
		return config.getMaxIdle();
	}

	/**
	 * @return
	 * @see org.apache.commons.pool2.impl.GenericObjectPoolConfig#getMaxTotal()
	 */
	public int getMaxTotal() {
		return config.getMaxTotal();
	}

	/**
	 * @return
	 * @see org.apache.commons.pool2.impl.BaseObjectPoolConfig#getMaxWaitMillis()
	 */
	public long getMaxWaitMillis() {
		return config.getMaxWaitMillis();
	}

	/**
	 * @return
	 * @see org.apache.commons.pool2.impl.GenericObjectPoolConfig#getMinIdle()
	 */
	public int getMinIdle() {
		return config.getMinIdle();
	}

	/**
	 * @return
	 * @see org.apache.commons.pool2.impl.BaseObjectPoolConfig#getTestOnBorrow()
	 */
	public boolean getTestOnBorrow() {
		return config.getTestOnBorrow();
	}

	/**
	 * @return
	 * @see org.apache.commons.pool2.impl.BaseObjectPoolConfig#getTestOnCreate()
	 */
	public boolean getTestOnCreate() {
		return config.getTestOnCreate();
	}

	/**
	 * @return
	 * @see org.apache.commons.pool2.impl.BaseObjectPoolConfig#getTestOnReturn()
	 */
	public boolean getTestOnReturn() {
		return config.getTestOnReturn();
	}

	/**
	 * @return
	 * @see org.apache.commons.pool2.impl.BaseObjectPoolConfig#getTestWhileIdle()
	 */
	public boolean getTestWhileIdle() {
		return config.getTestWhileIdle();
	}

	/**
	 * @param maxIdle
	 * @see org.apache.commons.pool2.impl.GenericObjectPoolConfig#setMaxIdle(int)
	 */
	public void setMaxIdle(int maxIdle) {
		config.setMaxIdle(maxIdle);
	}

	/**
	 * @param maxTotal
	 * @see org.apache.commons.pool2.impl.GenericObjectPoolConfig#setMaxTotal(int)
	 */
	public void setMaxTotal(int maxTotal) {
		config.setMaxTotal(maxTotal);
	}

	/**
	 * @param maxWaitMillis
	 * @see org.apache.commons.pool2.impl.BaseObjectPoolConfig#setMaxWaitMillis(long)
	 */
	public void setMaxWaitMillis(long maxWaitMillis) {
		config.setMaxWaitMillis(maxWaitMillis);
	}

	/**
	 * @param minIdle
	 * @see org.apache.commons.pool2.impl.GenericObjectPoolConfig#setMinIdle(int)
	 */
	public void setMinIdle(int minIdle) {
		config.setMinIdle(minIdle);
	}

	/**
	 * @param testOnBorrow
	 * @see org.apache.commons.pool2.impl.BaseObjectPoolConfig#setTestOnBorrow(boolean)
	 */
	public void setTestOnBorrow(boolean testOnBorrow) {
		config.setTestOnBorrow(testOnBorrow);
	}

	/**
	 * @param testOnCreate
	 * @see org.apache.commons.pool2.impl.BaseObjectPoolConfig#setTestOnCreate(boolean)
	 */
	public void setTestOnCreate(boolean testOnCreate) {
		config.setTestOnCreate(testOnCreate);
	}

	/**
	 * @param testOnReturn
	 * @see org.apache.commons.pool2.impl.BaseObjectPoolConfig#setTestOnReturn(boolean)
	 */
	public void setTestOnReturn(boolean testOnReturn) {
		config.setTestOnReturn(testOnReturn);
	}

	/**
	 * @param testWhileIdle
	 * @see org.apache.commons.pool2.impl.BaseObjectPoolConfig#setTestWhileIdle(boolean)
	 */
	public void setTestWhileIdle(boolean testWhileIdle) {
		config.setTestWhileIdle(testWhileIdle);
	}
	//--------------------------------------------------------------------------
	
	public RedisConnection getConnection(){
		RedisConnection conn=new RedisConnection();
		conn.jedis=pool.getResource();
		return conn;
	}
	//--------------------------------------------------------------------------
	//
	@Override
	public void init() throws Exception {
		pool=new ShardedJedisPool(config, shardInfos,Hashing.MURMUR_HASH);
		//
		ConsoleServer cs=Jazmin.getServer(ConsoleServer.class);
		if(cs!=null){
			cs.registerCommand(new RedisDriverCommand());
		}
	}
	//
	@Override
	public void stop() throws Exception {
		pool.destroy();
	}
	//
	@Override
	public String info() {
		InfoBuilder ib= InfoBuilder.create().format("%-30s:%-30s\n")
				.print("maxTotal",getMaxTotal())
				.print("maxIdle",getMaxIdle())
				.print("minIdle",getMinIdle())
				.print("maxWaitMillis",getMaxWaitMillis())
				.print("testOnBorrow",getTestOnBorrow())
				.print("testOnCreate",getTestOnCreate())
				.print("testOnReturn",getTestOnReturn())
				.print("testWhileIdle",getTestWhileIdle());
		//
		ib.section("shards");
		int index=0;
		for(JedisShardInfo s:shardInfos){
			ib.print("shard-"+(index++),s.getHost()+":"+s.getPort()+"/"+s.getName());						
		}	
		//
		return ib.toString();
	}
}
