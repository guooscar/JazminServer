/**
 * 
 */
package jazmin.driver.redis;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import redis.clients.jedis.BinaryClient.LIST_POSITION;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ScanResult;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPipeline;
import redis.clients.jedis.SortingParams;
import redis.clients.jedis.Tuple;
import redis.clients.util.Pool;

/**
 * @author g2131
 *
 */
public class RedisConnection {
	private static AtomicLong connectCounter=new AtomicLong();
	ShardedJedis jedis;
	String name="";
	public RedisConnection() {
		name="RedisConnection-"+connectCounter.incrementAndGet();
	}
	//
	public String getName(){
		return name;
	}
	//
	//
	/**
	 * @param key
	 * @param value
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#append(byte[], byte[])
	 */
	public Long append(byte[] key, byte[] value) {
		return jedis.append(key, value);
	}

	/**
	 * @param key
	 * @param value
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#append(java.lang.String, java.lang.String)
	 */
	public Long append(String key, String value) {
		return jedis.append(key, value);
	}

	/**
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#bitcount(byte[], long, long)
	 */
	public Long bitcount(byte[] key, long start, long end) {
		return jedis.bitcount(key, start, end);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#bitcount(byte[])
	 */
	public Long bitcount(byte[] key) {
		return jedis.bitcount(key);
	}

	/**
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#bitcount(java.lang.String, long, long)
	 */
	public Long bitcount(String key, long start, long end) {
		return jedis.bitcount(key, start, end);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#bitcount(java.lang.String)
	 */
	public Long bitcount(String key) {
		return jedis.bitcount(key);
	}

	/**
	 * @param arg
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#blpop(byte[])
	 */
	public List<byte[]> blpop(byte[] arg) {
		return jedis.blpop(arg);
	}

	/**
	 * @param timeout
	 * @param key
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#blpop(int, java.lang.String)
	 */
	public List<String> blpop(int timeout, String key) {
		return jedis.blpop(timeout, key);
	}

	/**
	 * @param arg
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#blpop(java.lang.String)
	 */
	public List<String> blpop(String arg) {
		return jedis.blpop(arg);
	}

	/**
	 * @param arg
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#brpop(byte[])
	 */
	public List<byte[]> brpop(byte[] arg) {
		return jedis.brpop(arg);
	}

	/**
	 * @param timeout
	 * @param key
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#brpop(int, java.lang.String)
	 */
	public List<String> brpop(int timeout, String key) {
		return jedis.brpop(timeout, key);
	}

	/**
	 * @param arg
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#brpop(java.lang.String)
	 */
	public List<String> brpop(String arg) {
		return jedis.brpop(arg);
	}

	/**
	 * 
	 * @see redis.clients.jedis.ShardedJedis#close()
	 */
	public void close() {
		jedis.close();
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#decr(byte[])
	 */
	public Long decr(byte[] key) {
		return jedis.decr(key);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#decr(java.lang.String)
	 */
	public Long decr(String key) {
		return jedis.decr(key);
	}

	/**
	 * @param key
	 * @param integer
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#decrBy(byte[], long)
	 */
	public Long decrBy(byte[] key, long integer) {
		return jedis.decrBy(key, integer);
	}

	/**
	 * @param key
	 * @param integer
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#decrBy(java.lang.String, long)
	 */
	public Long decrBy(String key, long integer) {
		return jedis.decrBy(key, integer);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#del(byte[])
	 */
	public Long del(byte[] key) {
		return jedis.del(key);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#del(java.lang.String)
	 */
	public Long del(String key) {
		return jedis.del(key);
	}

	/**
	 * 
	 * @see redis.clients.jedis.BinaryShardedJedis#disconnect()
	 */
	public void disconnect() {
		jedis.disconnect();
	}

	/**
	 * @param arg
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#echo(byte[])
	 */
	public byte[] echo(byte[] arg) {
		return jedis.echo(arg);
	}

	/**
	 * @param string
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#echo(java.lang.String)
	 */
	public String echo(String string) {
		return jedis.echo(string);
	}

	/**
	 * @param obj
	 * @return
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		return jedis.equals(obj);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#exists(byte[])
	 */
	public Boolean exists(byte[] key) {
		return jedis.exists(key);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#exists(java.lang.String)
	 */
	public Boolean exists(String key) {
		return jedis.exists(key);
	}

	/**
	 * @param key
	 * @param seconds
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#expire(byte[], int)
	 */
	public Long expire(byte[] key, int seconds) {
		return jedis.expire(key, seconds);
	}

	/**
	 * @param key
	 * @param seconds
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#expire(java.lang.String, int)
	 */
	public Long expire(String key, int seconds) {
		return jedis.expire(key, seconds);
	}

	/**
	 * @param key
	 * @param unixTime
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#expireAt(byte[], long)
	 */
	public Long expireAt(byte[] key, long unixTime) {
		return jedis.expireAt(key, unixTime);
	}

	/**
	 * @param key
	 * @param unixTime
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#expireAt(java.lang.String, long)
	 */
	public Long expireAt(String key, long unixTime) {
		return jedis.expireAt(key, unixTime);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#get(byte[])
	 */
	public byte[] get(byte[] key) {
		return jedis.get(key);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#get(java.lang.String)
	 */
	public String get(String key) {
		return jedis.get(key);
	}

	/**
	 * @return
	 * @see redis.clients.util.Sharded#getAllShardInfo()
	 */
	public Collection<JedisShardInfo> getAllShardInfo() {
		return jedis.getAllShardInfo();
	}

	/**
	 * @return
	 * @see redis.clients.util.Sharded#getAllShards()
	 */
	public Collection<Jedis> getAllShards() {
		return jedis.getAllShards();
	}

	/**
	 * @param arg0
	 * @return
	 * @see redis.clients.util.Sharded#getKeyTag(java.lang.String)
	 */
	public String getKeyTag(String arg0) {
		return jedis.getKeyTag(arg0);
	}

	/**
	 * @param key
	 * @param value
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#getSet(byte[], byte[])
	 */
	public byte[] getSet(byte[] key, byte[] value) {
		return jedis.getSet(key, value);
	}

	/**
	 * @param key
	 * @param value
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#getSet(java.lang.String, java.lang.String)
	 */
	public String getSet(String key, String value) {
		return jedis.getSet(key, value);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.util.Sharded#getShard(byte[])
	 */
	public Jedis getShard(byte[] key) {
		return jedis.getShard(key);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.util.Sharded#getShard(java.lang.String)
	 */
	public Jedis getShard(String key) {
		return jedis.getShard(key);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.util.Sharded#getShardInfo(byte[])
	 */
	public JedisShardInfo getShardInfo(byte[] key) {
		return jedis.getShardInfo(key);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.util.Sharded#getShardInfo(java.lang.String)
	 */
	public JedisShardInfo getShardInfo(String key) {
		return jedis.getShardInfo(key);
	}

	/**
	 * @param key
	 * @param offset
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#getbit(byte[], long)
	 */
	public Boolean getbit(byte[] key, long offset) {
		return jedis.getbit(key, offset);
	}

	/**
	 * @param key
	 * @param offset
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#getbit(java.lang.String, long)
	 */
	public Boolean getbit(String key, long offset) {
		return jedis.getbit(key, offset);
	}

	/**
	 * @param key
	 * @param startOffset
	 * @param endOffset
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#getrange(byte[], long, long)
	 */
	public byte[] getrange(byte[] key, long startOffset, long endOffset) {
		return jedis.getrange(key, startOffset, endOffset);
	}

	/**
	 * @param key
	 * @param startOffset
	 * @param endOffset
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#getrange(java.lang.String, long, long)
	 */
	public String getrange(String key, long startOffset, long endOffset) {
		return jedis.getrange(key, startOffset, endOffset);
	}

	/**
	 * @return
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return jedis.hashCode();
	}

	/**
	 * @param key
	 * @param fields
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#hdel(byte[], byte[][])
	 */
	public Long hdel(byte[] key, byte[]... fields) {
		return jedis.hdel(key, fields);
	}

	/**
	 * @param key
	 * @param fields
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#hdel(java.lang.String, java.lang.String[])
	 */
	public Long hdel(String key, String... fields) {
		return jedis.hdel(key, fields);
	}

	/**
	 * @param key
	 * @param field
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#hexists(byte[], byte[])
	 */
	public Boolean hexists(byte[] key, byte[] field) {
		return jedis.hexists(key, field);
	}

	/**
	 * @param key
	 * @param field
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#hexists(java.lang.String, java.lang.String)
	 */
	public Boolean hexists(String key, String field) {
		return jedis.hexists(key, field);
	}

	/**
	 * @param key
	 * @param field
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#hget(byte[], byte[])
	 */
	public byte[] hget(byte[] key, byte[] field) {
		return jedis.hget(key, field);
	}

	/**
	 * @param key
	 * @param field
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#hget(java.lang.String, java.lang.String)
	 */
	public String hget(String key, String field) {
		return jedis.hget(key, field);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#hgetAll(byte[])
	 */
	public Map<byte[], byte[]> hgetAll(byte[] key) {
		return jedis.hgetAll(key);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#hgetAll(java.lang.String)
	 */
	public Map<String, String> hgetAll(String key) {
		return jedis.hgetAll(key);
	}

	/**
	 * @param key
	 * @param field
	 * @param value
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#hincrBy(byte[], byte[], long)
	 */
	public Long hincrBy(byte[] key, byte[] field, long value) {
		return jedis.hincrBy(key, field, value);
	}

	/**
	 * @param key
	 * @param field
	 * @param value
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#hincrBy(java.lang.String, java.lang.String, long)
	 */
	public Long hincrBy(String key, String field, long value) {
		return jedis.hincrBy(key, field, value);
	}

	/**
	 * @param key
	 * @param field
	 * @param value
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#hincrByFloat(byte[], byte[], double)
	 */
	public Double hincrByFloat(byte[] key, byte[] field, double value) {
		return jedis.hincrByFloat(key, field, value);
	}

	/**
	 * @param key
	 * @param field
	 * @param value
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#hincrByFloat(java.lang.String, java.lang.String, double)
	 */
	public Double hincrByFloat(String key, String field, double value) {
		return jedis.hincrByFloat(key, field, value);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#hkeys(byte[])
	 */
	public Set<byte[]> hkeys(byte[] key) {
		return jedis.hkeys(key);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#hkeys(java.lang.String)
	 */
	public Set<String> hkeys(String key) {
		return jedis.hkeys(key);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#hlen(byte[])
	 */
	public Long hlen(byte[] key) {
		return jedis.hlen(key);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#hlen(java.lang.String)
	 */
	public Long hlen(String key) {
		return jedis.hlen(key);
	}

	/**
	 * @param key
	 * @param fields
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#hmget(byte[], byte[][])
	 */
	public List<byte[]> hmget(byte[] key, byte[]... fields) {
		return jedis.hmget(key, fields);
	}

	/**
	 * @param key
	 * @param fields
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#hmget(java.lang.String, java.lang.String[])
	 */
	public List<String> hmget(String key, String... fields) {
		return jedis.hmget(key, fields);
	}

	/**
	 * @param key
	 * @param hash
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#hmset(byte[], java.util.Map)
	 */
	public String hmset(byte[] key, Map<byte[], byte[]> hash) {
		return jedis.hmset(key, hash);
	}

	/**
	 * @param key
	 * @param hash
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#hmset(java.lang.String, java.util.Map)
	 */
	public String hmset(String key, Map<String, String> hash) {
		return jedis.hmset(key, hash);
	}

	/**
	 * @param key
	 * @param cursor
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#hscan(java.lang.String, java.lang.String)
	 */
	public ScanResult<Entry<String, String>> hscan(String key, String cursor) {
		return jedis.hscan(key, cursor);
	}

	/**
	 * @param key
	 * @param field
	 * @param value
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#hset(byte[], byte[], byte[])
	 */
	public Long hset(byte[] key, byte[] field, byte[] value) {
		return jedis.hset(key, field, value);
	}

	/**
	 * @param key
	 * @param field
	 * @param value
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#hset(java.lang.String, java.lang.String, java.lang.String)
	 */
	public Long hset(String key, String field, String value) {
		return jedis.hset(key, field, value);
	}

	/**
	 * @param key
	 * @param field
	 * @param value
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#hsetnx(byte[], byte[], byte[])
	 */
	public Long hsetnx(byte[] key, byte[] field, byte[] value) {
		return jedis.hsetnx(key, field, value);
	}

	/**
	 * @param key
	 * @param field
	 * @param value
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#hsetnx(java.lang.String, java.lang.String, java.lang.String)
	 */
	public Long hsetnx(String key, String field, String value) {
		return jedis.hsetnx(key, field, value);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#hvals(byte[])
	 */
	public Collection<byte[]> hvals(byte[] key) {
		return jedis.hvals(key);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#hvals(java.lang.String)
	 */
	public List<String> hvals(String key) {
		return jedis.hvals(key);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#incr(byte[])
	 */
	public Long incr(byte[] key) {
		return jedis.incr(key);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#incr(java.lang.String)
	 */
	public Long incr(String key) {
		return jedis.incr(key);
	}

	/**
	 * @param key
	 * @param integer
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#incrBy(byte[], long)
	 */
	public Long incrBy(byte[] key, long integer) {
		return jedis.incrBy(key, integer);
	}

	/**
	 * @param key
	 * @param integer
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#incrBy(java.lang.String, long)
	 */
	public Long incrBy(String key, long integer) {
		return jedis.incrBy(key, integer);
	}

	/**
	 * @param key
	 * @param integer
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#incrByFloat(byte[], double)
	 */
	public Double incrByFloat(byte[] key, double integer) {
		return jedis.incrByFloat(key, integer);
	}

	/**
	 * @param key
	 * @param integer
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#incrByFloat(java.lang.String, double)
	 */
	public Double incrByFloat(String key, double integer) {
		return jedis.incrByFloat(key, integer);
	}

	/**
	 * @param key
	 * @param index
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#lindex(byte[], long)
	 */
	public byte[] lindex(byte[] key, long index) {
		return jedis.lindex(key, index);
	}

	/**
	 * @param key
	 * @param index
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#lindex(java.lang.String, long)
	 */
	public String lindex(String key, long index) {
		return jedis.lindex(key, index);
	}

	/**
	 * @param key
	 * @param where
	 * @param pivot
	 * @param value
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#linsert(byte[], redis.clients.jedis.BinaryClient.LIST_POSITION, byte[], byte[])
	 */
	public Long linsert(byte[] key, LIST_POSITION where, byte[] pivot,
			byte[] value) {
		return jedis.linsert(key, where, pivot, value);
	}

	/**
	 * @param key
	 * @param where
	 * @param pivot
	 * @param value
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#linsert(java.lang.String, redis.clients.jedis.BinaryClient.LIST_POSITION, java.lang.String, java.lang.String)
	 */
	public Long linsert(String key, LIST_POSITION where, String pivot,
			String value) {
		return jedis.linsert(key, where, pivot, value);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#llen(byte[])
	 */
	public Long llen(byte[] key) {
		return jedis.llen(key);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#llen(java.lang.String)
	 */
	public Long llen(String key) {
		return jedis.llen(key);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#lpop(byte[])
	 */
	public byte[] lpop(byte[] key) {
		return jedis.lpop(key);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#lpop(java.lang.String)
	 */
	public String lpop(String key) {
		return jedis.lpop(key);
	}

	/**
	 * @param key
	 * @param strings
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#lpush(byte[], byte[][])
	 */
	public Long lpush(byte[] key, byte[]... strings) {
		return jedis.lpush(key, strings);
	}

	/**
	 * @param key
	 * @param strings
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#lpush(java.lang.String, java.lang.String[])
	 */
	public Long lpush(String key, String... strings) {
		return jedis.lpush(key, strings);
	}

	/**
	 * @param key
	 * @param string
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#lpushx(byte[], byte[][])
	 */
	public Long lpushx(byte[] key, byte[]... string) {
		return jedis.lpushx(key, string);
	}

	/**
	 * @param key
	 * @param string
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#lpushx(java.lang.String, java.lang.String[])
	 */
	public Long lpushx(String key, String... string) {
		return jedis.lpushx(key, string);
	}

	/**
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#lrange(byte[], long, long)
	 */
	public List<byte[]> lrange(byte[] key, long start, long end) {
		return jedis.lrange(key, start, end);
	}

	/**
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#lrange(java.lang.String, long, long)
	 */
	public List<String> lrange(String key, long start, long end) {
		return jedis.lrange(key, start, end);
	}

	/**
	 * @param key
	 * @param count
	 * @param value
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#lrem(byte[], long, byte[])
	 */
	public Long lrem(byte[] key, long count, byte[] value) {
		return jedis.lrem(key, count, value);
	}

	/**
	 * @param key
	 * @param count
	 * @param value
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#lrem(java.lang.String, long, java.lang.String)
	 */
	public Long lrem(String key, long count, String value) {
		return jedis.lrem(key, count, value);
	}

	/**
	 * @param key
	 * @param index
	 * @param value
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#lset(byte[], long, byte[])
	 */
	public String lset(byte[] key, long index, byte[] value) {
		return jedis.lset(key, index, value);
	}

	/**
	 * @param key
	 * @param index
	 * @param value
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#lset(java.lang.String, long, java.lang.String)
	 */
	public String lset(String key, long index, String value) {
		return jedis.lset(key, index, value);
	}

	/**
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#ltrim(byte[], long, long)
	 */
	public String ltrim(byte[] key, long start, long end) {
		return jedis.ltrim(key, start, end);
	}

	/**
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#ltrim(java.lang.String, long, long)
	 */
	public String ltrim(String key, long start, long end) {
		return jedis.ltrim(key, start, end);
	}

	/**
	 * @param key
	 * @param dbIndex
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#move(byte[], int)
	 */
	public Long move(byte[] key, int dbIndex) {
		return jedis.move(key, dbIndex);
	}

	/**
	 * @param key
	 * @param dbIndex
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#move(java.lang.String, int)
	 */
	public Long move(String key, int dbIndex) {
		return jedis.move(key, dbIndex);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#objectEncoding(byte[])
	 */
	public byte[] objectEncoding(byte[] key) {
		return jedis.objectEncoding(key);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#objectIdletime(byte[])
	 */
	public Long objectIdletime(byte[] key) {
		return jedis.objectIdletime(key);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#objectRefcount(byte[])
	 */
	public Long objectRefcount(byte[] key) {
		return jedis.objectRefcount(key);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#persist(byte[])
	 */
	public Long persist(byte[] key) {
		return jedis.persist(key);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#persist(java.lang.String)
	 */
	public Long persist(String key) {
		return jedis.persist(key);
	}

	/**
	 * @param key
	 * @param milliseconds
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#pexpire(byte[], long)
	 */
	public Long pexpire(byte[] key, long milliseconds) {
		return jedis.pexpire(key, milliseconds);
	}

	/**
	 * @param key
	 * @param milliseconds
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#pexpire(java.lang.String, long)
	 */
	public Long pexpire(String key, long milliseconds) {
		return jedis.pexpire(key, milliseconds);
	}

	/**
	 * @param key
	 * @param millisecondsTimestamp
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#pexpireAt(byte[], long)
	 */
	public Long pexpireAt(byte[] key, long millisecondsTimestamp) {
		return jedis.pexpireAt(key, millisecondsTimestamp);
	}

	/**
	 * @param key
	 * @param millisecondsTimestamp
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#pexpireAt(java.lang.String, long)
	 */
	public Long pexpireAt(String key, long millisecondsTimestamp) {
		return jedis.pexpireAt(key, millisecondsTimestamp);
	}

	/**
	 * @param key
	 * @param elements
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#pfadd(byte[], byte[][])
	 */
	public Long pfadd(byte[] key, byte[]... elements) {
		return jedis.pfadd(key, elements);
	}

	/**
	 * @param key
	 * @param elements
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#pfadd(java.lang.String, java.lang.String[])
	 */
	public Long pfadd(String key, String... elements) {
		return jedis.pfadd(key, elements);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#pfcount(byte[])
	 */
	public long pfcount(byte[] key) {
		return jedis.pfcount(key);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#pfcount(java.lang.String)
	 */
	public long pfcount(String key) {
		return jedis.pfcount(key);
	}

	/**
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#pipelined()
	 */
	public ShardedJedisPipeline pipelined() {
		return jedis.pipelined();
	}

	

	/**
	 * 
	 * @see redis.clients.jedis.ShardedJedis#resetState()
	 */
	public void resetState() {
		jedis.resetState();
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#rpop(byte[])
	 */
	public byte[] rpop(byte[] key) {
		return jedis.rpop(key);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#rpop(java.lang.String)
	 */
	public String rpop(String key) {
		return jedis.rpop(key);
	}

	/**
	 * @param key
	 * @param strings
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#rpush(byte[], byte[][])
	 */
	public Long rpush(byte[] key, byte[]... strings) {
		return jedis.rpush(key, strings);
	}

	/**
	 * @param key
	 * @param strings
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#rpush(java.lang.String, java.lang.String[])
	 */
	public Long rpush(String key, String... strings) {
		return jedis.rpush(key, strings);
	}

	/**
	 * @param key
	 * @param string
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#rpushx(byte[], byte[][])
	 */
	public Long rpushx(byte[] key, byte[]... string) {
		return jedis.rpushx(key, string);
	}

	/**
	 * @param key
	 * @param string
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#rpushx(java.lang.String, java.lang.String[])
	 */
	public Long rpushx(String key, String... string) {
		return jedis.rpushx(key, string);
	}

	/**
	 * @param key
	 * @param members
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#sadd(byte[], byte[][])
	 */
	public Long sadd(byte[] key, byte[]... members) {
		return jedis.sadd(key, members);
	}

	/**
	 * @param key
	 * @param members
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#sadd(java.lang.String, java.lang.String[])
	 */
	public Long sadd(String key, String... members) {
		return jedis.sadd(key, members);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#scard(byte[])
	 */
	public Long scard(byte[] key) {
		return jedis.scard(key);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#scard(java.lang.String)
	 */
	public Long scard(String key) {
		return jedis.scard(key);
	}

	/**
	 * @param key
	 * @param value
	 * @param nxxx
	 * @param expx
	 * @param time
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#set(byte[], byte[], byte[], byte[], long)
	 */
	public String set(byte[] key, byte[] value, byte[] nxxx, byte[] expx,
			long time) {
		return jedis.set(key, value, nxxx, expx, time);
	}

	/**
	 * @param key
	 * @param value
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#set(byte[], byte[])
	 */
	public String set(byte[] key, byte[] value) {
		return jedis.set(key, value);
	}

	/**
	 * @param key
	 * @param value
	 * @param nxxx
	 * @param expx
	 * @param time
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#set(java.lang.String, java.lang.String, java.lang.String, java.lang.String, long)
	 */
	public String set(String key, String value, String nxxx, String expx,
			long time) {
		return jedis.set(key, value, nxxx, expx, time);
	}

	/**
	 * @param key
	 * @param value
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#set(java.lang.String, java.lang.String)
	 */
	public String set(String key, String value) {
		return jedis.set(key, value);
	}

	/**
	 * @param shardedJedisPool
	 * @see redis.clients.jedis.ShardedJedis#setDataSource(redis.clients.util.Pool)
	 */
	public void setDataSource(Pool<ShardedJedis> shardedJedisPool) {
		jedis.setDataSource(shardedJedisPool);
	}

	/**
	 * @param key
	 * @param offset
	 * @param value
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#setbit(byte[], long, boolean)
	 */
	public Boolean setbit(byte[] key, long offset, boolean value) {
		return jedis.setbit(key, offset, value);
	}

	/**
	 * @param key
	 * @param offset
	 * @param value
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#setbit(byte[], long, byte[])
	 */
	public Boolean setbit(byte[] key, long offset, byte[] value) {
		return jedis.setbit(key, offset, value);
	}

	/**
	 * @param key
	 * @param offset
	 * @param value
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#setbit(java.lang.String, long, boolean)
	 */
	public Boolean setbit(String key, long offset, boolean value) {
		return jedis.setbit(key, offset, value);
	}

	/**
	 * @param key
	 * @param offset
	 * @param value
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#setbit(java.lang.String, long, java.lang.String)
	 */
	public Boolean setbit(String key, long offset, String value) {
		return jedis.setbit(key, offset, value);
	}

	/**
	 * @param key
	 * @param seconds
	 * @param value
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#setex(byte[], int, byte[])
	 */
	public String setex(byte[] key, int seconds, byte[] value) {
		return jedis.setex(key, seconds, value);
	}

	/**
	 * @param key
	 * @param seconds
	 * @param value
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#setex(java.lang.String, int, java.lang.String)
	 */
	public String setex(String key, int seconds, String value) {
		return jedis.setex(key, seconds, value);
	}

	/**
	 * @param key
	 * @param value
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#setnx(byte[], byte[])
	 */
	public Long setnx(byte[] key, byte[] value) {
		return jedis.setnx(key, value);
	}

	/**
	 * @param key
	 * @param value
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#setnx(java.lang.String, java.lang.String)
	 */
	public Long setnx(String key, String value) {
		return jedis.setnx(key, value);
	}

	/**
	 * @param key
	 * @param offset
	 * @param value
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#setrange(byte[], long, byte[])
	 */
	public Long setrange(byte[] key, long offset, byte[] value) {
		return jedis.setrange(key, offset, value);
	}

	/**
	 * @param key
	 * @param offset
	 * @param value
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#setrange(java.lang.String, long, java.lang.String)
	 */
	public Long setrange(String key, long offset, String value) {
		return jedis.setrange(key, offset, value);
	}

	/**
	 * @param key
	 * @param member
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#sismember(byte[], byte[])
	 */
	public Boolean sismember(byte[] key, byte[] member) {
		return jedis.sismember(key, member);
	}

	/**
	 * @param key
	 * @param member
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#sismember(java.lang.String, java.lang.String)
	 */
	public Boolean sismember(String key, String member) {
		return jedis.sismember(key, member);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#smembers(byte[])
	 */
	public Set<byte[]> smembers(byte[] key) {
		return jedis.smembers(key);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#smembers(java.lang.String)
	 */
	public Set<String> smembers(String key) {
		return jedis.smembers(key);
	}

	/**
	 * @param key
	 * @param sortingParameters
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#sort(byte[], redis.clients.jedis.SortingParams)
	 */
	public List<byte[]> sort(byte[] key, SortingParams sortingParameters) {
		return jedis.sort(key, sortingParameters);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#sort(byte[])
	 */
	public List<byte[]> sort(byte[] key) {
		return jedis.sort(key);
	}

	/**
	 * @param key
	 * @param sortingParameters
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#sort(java.lang.String, redis.clients.jedis.SortingParams)
	 */
	public List<String> sort(String key, SortingParams sortingParameters) {
		return jedis.sort(key, sortingParameters);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#sort(java.lang.String)
	 */
	public List<String> sort(String key) {
		return jedis.sort(key);
	}

	/**
	 * @param key
	 * @param count
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#spop(byte[], long)
	 */
	public Set<byte[]> spop(byte[] key, long count) {
		return jedis.spop(key, count);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#spop(byte[])
	 */
	public byte[] spop(byte[] key) {
		return jedis.spop(key);
	}

	/**
	 * @param key
	 * @param count
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#spop(java.lang.String, long)
	 */
	public Set<String> spop(String key, long count) {
		return jedis.spop(key, count);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#spop(java.lang.String)
	 */
	public String spop(String key) {
		return jedis.spop(key);
	}

	/**
	 * @param key
	 * @param count
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#srandmember(byte[], int)
	 */
	public List<?> srandmember(byte[] key, int count) {
		return jedis.srandmember(key, count);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#srandmember(byte[])
	 */
	public byte[] srandmember(byte[] key) {
		return jedis.srandmember(key);
	}

	/**
	 * @param key
	 * @param count
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#srandmember(java.lang.String, int)
	 */
	public List<String> srandmember(String key, int count) {
		return jedis.srandmember(key, count);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#srandmember(java.lang.String)
	 */
	public String srandmember(String key) {
		return jedis.srandmember(key);
	}

	/**
	 * @param key
	 * @param members
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#srem(byte[], byte[][])
	 */
	public Long srem(byte[] key, byte[]... members) {
		return jedis.srem(key, members);
	}

	/**
	 * @param key
	 * @param members
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#srem(java.lang.String, java.lang.String[])
	 */
	public Long srem(String key, String... members) {
		return jedis.srem(key, members);
	}



	/**
	 * @param key
	 * @param cursor
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#sscan(java.lang.String, java.lang.String)
	 */
	public ScanResult<String> sscan(String key, String cursor) {
		return jedis.sscan(key, cursor);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#strlen(byte[])
	 */
	public Long strlen(byte[] key) {
		return jedis.strlen(key);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#strlen(java.lang.String)
	 */
	public Long strlen(String key) {
		return jedis.strlen(key);
	}

	/**
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#substr(byte[], int, int)
	 */
	public byte[] substr(byte[] key, int start, int end) {
		return jedis.substr(key, start, end);
	}

	/**
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#substr(java.lang.String, int, int)
	 */
	public String substr(String key, int start, int end) {
		return jedis.substr(key, start, end);
	}

	/**
	 * @return
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return jedis.toString();
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#ttl(byte[])
	 */
	public Long ttl(byte[] key) {
		return jedis.ttl(key);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#ttl(java.lang.String)
	 */
	public Long ttl(String key) {
		return jedis.ttl(key);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#type(byte[])
	 */
	public String type(byte[] key) {
		return jedis.type(key);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#type(java.lang.String)
	 */
	public String type(String key) {
		return jedis.type(key);
	}

	/**
	 * @param key
	 * @param score
	 * @param member
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#zadd(byte[], double, byte[])
	 */
	public Long zadd(byte[] key, double score, byte[] member) {
		return jedis.zadd(key, score, member);
	}

	/**
	 * @param key
	 * @param scoreMembers
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#zadd(byte[], java.util.Map)
	 */
	public Long zadd(byte[] key, Map<byte[], Double> scoreMembers) {
		return jedis.zadd(key, scoreMembers);
	}

	/**
	 * @param key
	 * @param score
	 * @param member
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#zadd(java.lang.String, double, java.lang.String)
	 */
	public Long zadd(String key, double score, String member) {
		return jedis.zadd(key, score, member);
	}

	/**
	 * @param key
	 * @param scoreMembers
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#zadd(java.lang.String, java.util.Map)
	 */
	public Long zadd(String key, Map<String, Double> scoreMembers) {
		return jedis.zadd(key, scoreMembers);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#zcard(byte[])
	 */
	public Long zcard(byte[] key) {
		return jedis.zcard(key);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#zcard(java.lang.String)
	 */
	public Long zcard(String key) {
		return jedis.zcard(key);
	}

	/**
	 * @param key
	 * @param min
	 * @param max
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#zcount(byte[], byte[], byte[])
	 */
	public Long zcount(byte[] key, byte[] min, byte[] max) {
		return jedis.zcount(key, min, max);
	}

	/**
	 * @param key
	 * @param min
	 * @param max
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#zcount(byte[], double, double)
	 */
	public Long zcount(byte[] key, double min, double max) {
		return jedis.zcount(key, min, max);
	}

	/**
	 * @param key
	 * @param min
	 * @param max
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#zcount(java.lang.String, double, double)
	 */
	public Long zcount(String key, double min, double max) {
		return jedis.zcount(key, min, max);
	}

	/**
	 * @param key
	 * @param min
	 * @param max
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#zcount(java.lang.String, java.lang.String, java.lang.String)
	 */
	public Long zcount(String key, String min, String max) {
		return jedis.zcount(key, min, max);
	}

	/**
	 * @param key
	 * @param score
	 * @param member
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#zincrby(byte[], double, byte[])
	 */
	public Double zincrby(byte[] key, double score, byte[] member) {
		return jedis.zincrby(key, score, member);
	}

	/**
	 * @param key
	 * @param score
	 * @param member
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#zincrby(java.lang.String, double, java.lang.String)
	 */
	public Double zincrby(String key, double score, String member) {
		return jedis.zincrby(key, score, member);
	}

	/**
	 * @param key
	 * @param min
	 * @param max
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#zlexcount(byte[], byte[], byte[])
	 */
	public Long zlexcount(byte[] key, byte[] min, byte[] max) {
		return jedis.zlexcount(key, min, max);
	}

	/**
	 * @param key
	 * @param min
	 * @param max
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#zlexcount(java.lang.String, java.lang.String, java.lang.String)
	 */
	public Long zlexcount(String key, String min, String max) {
		return jedis.zlexcount(key, min, max);
	}

	/**
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#zrange(byte[], long, long)
	 */
	public Set<byte[]> zrange(byte[] key, long start, long end) {
		return jedis.zrange(key, start, end);
	}

	/**
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#zrange(java.lang.String, long, long)
	 */
	public Set<String> zrange(String key, long start, long end) {
		return jedis.zrange(key, start, end);
	}

	/**
	 * @param key
	 * @param min
	 * @param max
	 * @param offset
	 * @param count
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#zrangeByLex(byte[], byte[], byte[], int, int)
	 */
	public Set<byte[]> zrangeByLex(byte[] key, byte[] min, byte[] max,
			int offset, int count) {
		return jedis.zrangeByLex(key, min, max, offset, count);
	}

	/**
	 * @param key
	 * @param min
	 * @param max
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#zrangeByLex(byte[], byte[], byte[])
	 */
	public Set<byte[]> zrangeByLex(byte[] key, byte[] min, byte[] max) {
		return jedis.zrangeByLex(key, min, max);
	}

	/**
	 * @param key
	 * @param min
	 * @param max
	 * @param offset
	 * @param count
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#zrangeByLex(java.lang.String, java.lang.String, java.lang.String, int, int)
	 */
	public Set<String> zrangeByLex(String key, String min, String max,
			int offset, int count) {
		return jedis.zrangeByLex(key, min, max, offset, count);
	}

	/**
	 * @param key
	 * @param min
	 * @param max
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#zrangeByLex(java.lang.String, java.lang.String, java.lang.String)
	 */
	public Set<String> zrangeByLex(String key, String min, String max) {
		return jedis.zrangeByLex(key, min, max);
	}

	/**
	 * @param key
	 * @param min
	 * @param max
	 * @param offset
	 * @param count
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#zrangeByScore(byte[], byte[], byte[], int, int)
	 */
	public Set<byte[]> zrangeByScore(byte[] key, byte[] min, byte[] max,
			int offset, int count) {
		return jedis.zrangeByScore(key, min, max, offset, count);
	}

	/**
	 * @param key
	 * @param min
	 * @param max
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#zrangeByScore(byte[], byte[], byte[])
	 */
	public Set<byte[]> zrangeByScore(byte[] key, byte[] min, byte[] max) {
		return jedis.zrangeByScore(key, min, max);
	}

	/**
	 * @param key
	 * @param min
	 * @param max
	 * @param offset
	 * @param count
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#zrangeByScore(byte[], double, double, int, int)
	 */
	public Set<byte[]> zrangeByScore(byte[] key, double min, double max,
			int offset, int count) {
		return jedis.zrangeByScore(key, min, max, offset, count);
	}

	/**
	 * @param key
	 * @param min
	 * @param max
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#zrangeByScore(byte[], double, double)
	 */
	public Set<byte[]> zrangeByScore(byte[] key, double min, double max) {
		return jedis.zrangeByScore(key, min, max);
	}

	/**
	 * @param key
	 * @param min
	 * @param max
	 * @param offset
	 * @param count
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#zrangeByScore(java.lang.String, double, double, int, int)
	 */
	public Set<String> zrangeByScore(String key, double min, double max,
			int offset, int count) {
		return jedis.zrangeByScore(key, min, max, offset, count);
	}

	/**
	 * @param key
	 * @param min
	 * @param max
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#zrangeByScore(java.lang.String, double, double)
	 */
	public Set<String> zrangeByScore(String key, double min, double max) {
		return jedis.zrangeByScore(key, min, max);
	}

	/**
	 * @param key
	 * @param min
	 * @param max
	 * @param offset
	 * @param count
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#zrangeByScore(java.lang.String, java.lang.String, java.lang.String, int, int)
	 */
	public Set<String> zrangeByScore(String key, String min, String max,
			int offset, int count) {
		return jedis.zrangeByScore(key, min, max, offset, count);
	}

	/**
	 * @param key
	 * @param min
	 * @param max
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#zrangeByScore(java.lang.String, java.lang.String, java.lang.String)
	 */
	public Set<String> zrangeByScore(String key, String min, String max) {
		return jedis.zrangeByScore(key, min, max);
	}

	/**
	 * @param key
	 * @param min
	 * @param max
	 * @param offset
	 * @param count
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#zrangeByScoreWithScores(byte[], byte[], byte[], int, int)
	 */
	public Set<Tuple> zrangeByScoreWithScores(byte[] key, byte[] min,
			byte[] max, int offset, int count) {
		return jedis.zrangeByScoreWithScores(key, min, max, offset, count);
	}

	/**
	 * @param key
	 * @param min
	 * @param max
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#zrangeByScoreWithScores(byte[], byte[], byte[])
	 */
	public Set<Tuple> zrangeByScoreWithScores(byte[] key, byte[] min, byte[] max) {
		return jedis.zrangeByScoreWithScores(key, min, max);
	}

	/**
	 * @param key
	 * @param min
	 * @param max
	 * @param offset
	 * @param count
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#zrangeByScoreWithScores(byte[], double, double, int, int)
	 */
	public Set<Tuple> zrangeByScoreWithScores(byte[] key, double min,
			double max, int offset, int count) {
		return jedis.zrangeByScoreWithScores(key, min, max, offset, count);
	}

	/**
	 * @param key
	 * @param min
	 * @param max
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#zrangeByScoreWithScores(byte[], double, double)
	 */
	public Set<Tuple> zrangeByScoreWithScores(byte[] key, double min, double max) {
		return jedis.zrangeByScoreWithScores(key, min, max);
	}

	/**
	 * @param key
	 * @param min
	 * @param max
	 * @param offset
	 * @param count
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#zrangeByScoreWithScores(java.lang.String, double, double, int, int)
	 */
	public Set<Tuple> zrangeByScoreWithScores(String key, double min,
			double max, int offset, int count) {
		return jedis.zrangeByScoreWithScores(key, min, max, offset, count);
	}

	/**
	 * @param key
	 * @param min
	 * @param max
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#zrangeByScoreWithScores(java.lang.String, double, double)
	 */
	public Set<Tuple> zrangeByScoreWithScores(String key, double min, double max) {
		return jedis.zrangeByScoreWithScores(key, min, max);
	}

	/**
	 * @param key
	 * @param min
	 * @param max
	 * @param offset
	 * @param count
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#zrangeByScoreWithScores(java.lang.String, java.lang.String, java.lang.String, int, int)
	 */
	public Set<Tuple> zrangeByScoreWithScores(String key, String min,
			String max, int offset, int count) {
		return jedis.zrangeByScoreWithScores(key, min, max, offset, count);
	}

	/**
	 * @param key
	 * @param min
	 * @param max
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#zrangeByScoreWithScores(java.lang.String, java.lang.String, java.lang.String)
	 */
	public Set<Tuple> zrangeByScoreWithScores(String key, String min, String max) {
		return jedis.zrangeByScoreWithScores(key, min, max);
	}

	/**
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#zrangeWithScores(byte[], long, long)
	 */
	public Set<Tuple> zrangeWithScores(byte[] key, long start, long end) {
		return jedis.zrangeWithScores(key, start, end);
	}

	/**
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#zrangeWithScores(java.lang.String, long, long)
	 */
	public Set<Tuple> zrangeWithScores(String key, long start, long end) {
		return jedis.zrangeWithScores(key, start, end);
	}

	/**
	 * @param key
	 * @param member
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#zrank(byte[], byte[])
	 */
	public Long zrank(byte[] key, byte[] member) {
		return jedis.zrank(key, member);
	}

	/**
	 * @param key
	 * @param member
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#zrank(java.lang.String, java.lang.String)
	 */
	public Long zrank(String key, String member) {
		return jedis.zrank(key, member);
	}

	/**
	 * @param key
	 * @param members
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#zrem(byte[], byte[][])
	 */
	public Long zrem(byte[] key, byte[]... members) {
		return jedis.zrem(key, members);
	}

	/**
	 * @param key
	 * @param members
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#zrem(java.lang.String, java.lang.String[])
	 */
	public Long zrem(String key, String... members) {
		return jedis.zrem(key, members);
	}

	/**
	 * @param key
	 * @param min
	 * @param max
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#zremrangeByLex(byte[], byte[], byte[])
	 */
	public Long zremrangeByLex(byte[] key, byte[] min, byte[] max) {
		return jedis.zremrangeByLex(key, min, max);
	}

	/**
	 * @param key
	 * @param min
	 * @param max
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#zremrangeByLex(java.lang.String, java.lang.String, java.lang.String)
	 */
	public Long zremrangeByLex(String key, String min, String max) {
		return jedis.zremrangeByLex(key, min, max);
	}

	/**
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#zremrangeByRank(byte[], long, long)
	 */
	public Long zremrangeByRank(byte[] key, long start, long end) {
		return jedis.zremrangeByRank(key, start, end);
	}

	/**
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#zremrangeByRank(java.lang.String, long, long)
	 */
	public Long zremrangeByRank(String key, long start, long end) {
		return jedis.zremrangeByRank(key, start, end);
	}

	/**
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#zremrangeByScore(byte[], byte[], byte[])
	 */
	public Long zremrangeByScore(byte[] key, byte[] start, byte[] end) {
		return jedis.zremrangeByScore(key, start, end);
	}

	/**
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#zremrangeByScore(byte[], double, double)
	 */
	public Long zremrangeByScore(byte[] key, double start, double end) {
		return jedis.zremrangeByScore(key, start, end);
	}

	/**
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#zremrangeByScore(java.lang.String, double, double)
	 */
	public Long zremrangeByScore(String key, double start, double end) {
		return jedis.zremrangeByScore(key, start, end);
	}

	/**
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#zremrangeByScore(java.lang.String, java.lang.String, java.lang.String)
	 */
	public Long zremrangeByScore(String key, String start, String end) {
		return jedis.zremrangeByScore(key, start, end);
	}

	/**
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#zrevrange(byte[], long, long)
	 */
	public Set<byte[]> zrevrange(byte[] key, long start, long end) {
		return jedis.zrevrange(key, start, end);
	}

	/**
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#zrevrange(java.lang.String, long, long)
	 */
	public Set<String> zrevrange(String key, long start, long end) {
		return jedis.zrevrange(key, start, end);
	}

	/**
	 * @param key
	 * @param max
	 * @param min
	 * @param offset
	 * @param count
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#zrevrangeByLex(byte[], byte[], byte[], int, int)
	 */
	public Set<byte[]> zrevrangeByLex(byte[] key, byte[] max, byte[] min,
			int offset, int count) {
		return jedis.zrevrangeByLex(key, max, min, offset, count);
	}

	/**
	 * @param key
	 * @param max
	 * @param min
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#zrevrangeByLex(byte[], byte[], byte[])
	 */
	public Set<byte[]> zrevrangeByLex(byte[] key, byte[] max, byte[] min) {
		return jedis.zrevrangeByLex(key, max, min);
	}

	/**
	 * @param key
	 * @param max
	 * @param min
	 * @param offset
	 * @param count
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#zrevrangeByLex(java.lang.String, java.lang.String, java.lang.String, int, int)
	 */
	public Set<String> zrevrangeByLex(String key, String max, String min,
			int offset, int count) {
		return jedis.zrevrangeByLex(key, max, min, offset, count);
	}

	/**
	 * @param key
	 * @param max
	 * @param min
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#zrevrangeByLex(java.lang.String, java.lang.String, java.lang.String)
	 */
	public Set<String> zrevrangeByLex(String key, String max, String min) {
		return jedis.zrevrangeByLex(key, max, min);
	}

	/**
	 * @param key
	 * @param max
	 * @param min
	 * @param offset
	 * @param count
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#zrevrangeByScore(byte[], byte[], byte[], int, int)
	 */
	public Set<byte[]> zrevrangeByScore(byte[] key, byte[] max, byte[] min,
			int offset, int count) {
		return jedis.zrevrangeByScore(key, max, min, offset, count);
	}

	/**
	 * @param key
	 * @param max
	 * @param min
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#zrevrangeByScore(byte[], byte[], byte[])
	 */
	public Set<byte[]> zrevrangeByScore(byte[] key, byte[] max, byte[] min) {
		return jedis.zrevrangeByScore(key, max, min);
	}

	/**
	 * @param key
	 * @param max
	 * @param min
	 * @param offset
	 * @param count
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#zrevrangeByScore(byte[], double, double, int, int)
	 */
	public Set<byte[]> zrevrangeByScore(byte[] key, double max, double min,
			int offset, int count) {
		return jedis.zrevrangeByScore(key, max, min, offset, count);
	}

	/**
	 * @param key
	 * @param max
	 * @param min
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#zrevrangeByScore(byte[], double, double)
	 */
	public Set<byte[]> zrevrangeByScore(byte[] key, double max, double min) {
		return jedis.zrevrangeByScore(key, max, min);
	}

	/**
	 * @param key
	 * @param max
	 * @param min
	 * @param offset
	 * @param count
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#zrevrangeByScore(java.lang.String, double, double, int, int)
	 */
	public Set<String> zrevrangeByScore(String key, double max, double min,
			int offset, int count) {
		return jedis.zrevrangeByScore(key, max, min, offset, count);
	}

	/**
	 * @param key
	 * @param max
	 * @param min
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#zrevrangeByScore(java.lang.String, double, double)
	 */
	public Set<String> zrevrangeByScore(String key, double max, double min) {
		return jedis.zrevrangeByScore(key, max, min);
	}

	/**
	 * @param key
	 * @param max
	 * @param min
	 * @param offset
	 * @param count
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#zrevrangeByScore(java.lang.String, java.lang.String, java.lang.String, int, int)
	 */
	public Set<String> zrevrangeByScore(String key, String max, String min,
			int offset, int count) {
		return jedis.zrevrangeByScore(key, max, min, offset, count);
	}

	/**
	 * @param key
	 * @param max
	 * @param min
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#zrevrangeByScore(java.lang.String, java.lang.String, java.lang.String)
	 */
	public Set<String> zrevrangeByScore(String key, String max, String min) {
		return jedis.zrevrangeByScore(key, max, min);
	}

	/**
	 * @param key
	 * @param max
	 * @param min
	 * @param offset
	 * @param count
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#zrevrangeByScoreWithScores(byte[], byte[], byte[], int, int)
	 */
	public Set<Tuple> zrevrangeByScoreWithScores(byte[] key, byte[] max,
			byte[] min, int offset, int count) {
		return jedis.zrevrangeByScoreWithScores(key, max, min, offset, count);
	}

	/**
	 * @param key
	 * @param max
	 * @param min
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#zrevrangeByScoreWithScores(byte[], byte[], byte[])
	 */
	public Set<Tuple> zrevrangeByScoreWithScores(byte[] key, byte[] max,
			byte[] min) {
		return jedis.zrevrangeByScoreWithScores(key, max, min);
	}

	/**
	 * @param key
	 * @param max
	 * @param min
	 * @param offset
	 * @param count
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#zrevrangeByScoreWithScores(byte[], double, double, int, int)
	 */
	public Set<Tuple> zrevrangeByScoreWithScores(byte[] key, double max,
			double min, int offset, int count) {
		return jedis.zrevrangeByScoreWithScores(key, max, min, offset, count);
	}

	/**
	 * @param key
	 * @param max
	 * @param min
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#zrevrangeByScoreWithScores(byte[], double, double)
	 */
	public Set<Tuple> zrevrangeByScoreWithScores(byte[] key, double max,
			double min) {
		return jedis.zrevrangeByScoreWithScores(key, max, min);
	}

	/**
	 * @param key
	 * @param max
	 * @param min
	 * @param offset
	 * @param count
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#zrevrangeByScoreWithScores(java.lang.String, double, double, int, int)
	 */
	public Set<Tuple> zrevrangeByScoreWithScores(String key, double max,
			double min, int offset, int count) {
		return jedis.zrevrangeByScoreWithScores(key, max, min, offset, count);
	}

	/**
	 * @param key
	 * @param max
	 * @param min
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#zrevrangeByScoreWithScores(java.lang.String, double, double)
	 */
	public Set<Tuple> zrevrangeByScoreWithScores(String key, double max,
			double min) {
		return jedis.zrevrangeByScoreWithScores(key, max, min);
	}

	/**
	 * @param key
	 * @param max
	 * @param min
	 * @param offset
	 * @param count
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#zrevrangeByScoreWithScores(java.lang.String, java.lang.String, java.lang.String, int, int)
	 */
	public Set<Tuple> zrevrangeByScoreWithScores(String key, String max,
			String min, int offset, int count) {
		return jedis.zrevrangeByScoreWithScores(key, max, min, offset, count);
	}

	/**
	 * @param key
	 * @param max
	 * @param min
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#zrevrangeByScoreWithScores(java.lang.String, java.lang.String, java.lang.String)
	 */
	public Set<Tuple> zrevrangeByScoreWithScores(String key, String max,
			String min) {
		return jedis.zrevrangeByScoreWithScores(key, max, min);
	}

	/**
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#zrevrangeWithScores(byte[], long, long)
	 */
	public Set<Tuple> zrevrangeWithScores(byte[] key, long start, long end) {
		return jedis.zrevrangeWithScores(key, start, end);
	}

	/**
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#zrevrangeWithScores(java.lang.String, long, long)
	 */
	public Set<Tuple> zrevrangeWithScores(String key, long start, long end) {
		return jedis.zrevrangeWithScores(key, start, end);
	}

	/**
	 * @param key
	 * @param member
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#zrevrank(byte[], byte[])
	 */
	public Long zrevrank(byte[] key, byte[] member) {
		return jedis.zrevrank(key, member);
	}

	/**
	 * @param key
	 * @param member
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#zrevrank(java.lang.String, java.lang.String)
	 */
	public Long zrevrank(String key, String member) {
		return jedis.zrevrank(key, member);
	}

	

	/**
	 * @param key
	 * @param cursor
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#zscan(java.lang.String, java.lang.String)
	 */
	public ScanResult<Tuple> zscan(String key, String cursor) {
		return jedis.zscan(key, cursor);
	}

	/**
	 * @param key
	 * @param member
	 * @return
	 * @see redis.clients.jedis.BinaryShardedJedis#zscore(byte[], byte[])
	 */
	public Double zscore(byte[] key, byte[] member) {
		return jedis.zscore(key, member);
	}

	/**
	 * @param key
	 * @param member
	 * @return
	 * @see redis.clients.jedis.ShardedJedis#zscore(java.lang.String, java.lang.String)
	 */
	public Double zscore(String key, String member) {
		return jedis.zscore(key, member);
	}

}
