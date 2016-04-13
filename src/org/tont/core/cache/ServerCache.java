package org.tont.core.cache;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;


public class ServerCache {
	public static JedisPool pool;
	
	public static synchronized void initPool() {
		if (pool == null) {
			pool = new JedisPool(new JedisPoolConfig(), "172.21.60.189");
		}
	}
	
	public static void ping() {
		if (pool != null) {
			Jedis jedis = null;
			try {
				//连接 Redis 服务
				jedis = pool.getResource();
				// 查看服务是否运行
				System.out.println("Server is running: " + jedis.ping());
			} finally {
				if (jedis!=null) {
						jedis.close();
				}
			}
		} else {
			System.out.println("Pool is not inited!");
		}
	}
	
	public static Jedis getJedis() {
		Jedis jedis = null;
		try {
			jedis = (pool == null? null : pool.getResource());
		} catch (RuntimeException e) {
			if (e.getMessage().equals("Could not get a resource from the pool")) {
				System.out.println("Could not get a resource from the pool");
			} else {
				throw e;
			}
		}
		return jedis;
	}
	
	public static void closePool() {
		pool.destroy();
	}
	
}
