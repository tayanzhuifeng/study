package com.study.cache.transactions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import redis.clients.jedis.JedisCluster;

/**
 * Copyright: Copyright (c) 2018 Asiainfo
 * 
 * @ClassName: RedisClusterTransactionTest.java
 * @Description: 该类的功能描述
 *
 * @version: v1.0.0
 * @author: xz
 * @date: 2018年2月6日 上午1:22:42
 *
 *        Modification History: Date Author Version Description
 *        ---------------------------------------------------------* 2018年2月6日
 *        xz v1.0.0 修改原因
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:ApplicationContext.xml")
public class RedisClusterTransactionTest {
	@Resource
	private RedisTemplate<String, Object> redisTemplate;

	@Resource
	private JedisCluster jedisCluster;

	private ThreadLocal<SimpleDateFormat> simpleDateFormatThreadLocal = new ThreadLocal<SimpleDateFormat>() {
		@Override
		public SimpleDateFormat get() {
			return new SimpleDateFormat("yyyyMMdd");
		}
	};

	// hash tag

	/**
	 * 
	 * @Function: RedisClusterTransactionTest::testRedisTemplateCluster
	 * @Description: 测试redis template cluster
	 * @version: v1.0.0
	 * @author: xz
	 * @date: 2018年2月6日 下午10:48:59
	 *
	 *        Modification History: Date Author Version Description
	 *        -------------------------------------------------------------
	 */
	@Test
	public void testRedisTemplateCluster() {
		redisTemplate.opsForValue().set("aa", "300");
		redisTemplate.opsForValue().set("cc", "AA1");
		redisTemplate.opsForValue().set("t4", "BB");

		System.out.println(redisTemplate.opsForValue().get("aa"));
		System.out.println(redisTemplate.opsForValue().get("cc"));
		System.out.println(redisTemplate.opsForValue().get("t4"));
	}

	// redis template cluster lua script
	@Test
	public void testRedisTemplateClusterScript() {
		String key = "TEST" + simpleDateFormatThreadLocal.get().format(new Date());
		DefaultRedisScript<String> redisScript = new DefaultRedisScript<String>();
		redisScript.setLocation(new ClassPathResource("lua/genID.lua"));
		redisScript.setResultType(String.class);
		List<String> keys = new ArrayList<String>();

		keys.add(key);

		// 注意redis.clients.jedis.JedisCluster与org.springframework.data.redis.connection.jedis.JedisClusterConnection区别
		// JedisClusterConnection不支持lua脚本的执行，JedisCluster支持
		// 除了JedisCluster支持lua脚本的执行，lettuce也可以
		// 如果是集群模式，则JedisConnectionFactory则调用getClusterConnection，由于clusterConnection不支持evalsha，所以会报错
		// String nextId = redisTemplate.execute(redisScript, keys, new String());
		String nextId = redisTemplate.execute(redisScript, keys);

		System.out.println("生成的主键id：" + nextId);
	}

	/**
	 * 
	 * @Function: RedisClusterTransactionTest::testJedisClusterScript
	 * @Description: 测试RedisCluster lua脚本的执行
	 * @throws Exception
	 * @version: v1.0.0
	 * @author: xz
	 * @date: 2018年2月7日 上午12:45:22
	 *
	 *        Modification History: Date Author Version Description
	 *        -------------------------------------------------------------
	 */
	@Test
	public void testJedisClusterScript() throws Exception {

		/*
		 * String[] serverArray =
		 * "192.168.80.109:7000,192.168.80.109:7001,192.168.80.109:7002".split(",");
		 * Set<HostAndPort> nodes = new HashSet<HostAndPort>();
		 * 
		 * for (String ipPort : serverArray) { String[] ipPortPair = ipPort.split(":");
		 * nodes.add(new HostAndPort(ipPortPair[0].trim(),
		 * Integer.valueOf(ipPortPair[1].trim()))); }
		 * 
		 * JedisCluster jedisCluster = new JedisCluster(nodes);
		 */

		String sha = jedisCluster.scriptLoad("redis.call('set', KEYS[1], '100');", "test");

		List<String> listKey = new ArrayList<String>();
		List<String> listParam = new ArrayList<String>();
		listKey.add("test");
		System.out.println(jedisCluster.evalsha(sha, listKey, listParam));
		System.out.println(jedisCluster.get("test"));

		/*
		 * 其中 "return {KEYS[1],KEYS[2],ARGV[1],ARGV[2]}" 是被求值的 Lua 脚本，数字 2 指定了键名参数的数量，
		 * key1 和 key2 是键名参数，分别使用 KEYS[1] 和 KEYS[2] 访问，而最后的 first 和 second 则是附加参数，可以通过
		 * ARGV[1] 和 ARGV[2] 访问它们。
		 *
		 * 注意，这里一些操作不适用于redis-cluster，主要还是因为不同的key被分配到了不同的slot中
		 */
		Object eval = jedisCluster.eval("return {KEYS[1],ARGV[1],ARGV[2]}", 1, "lua", "key1", "dd");
		System.out.println(eval);

		// 脚本里使用的所有键都应该由 KEYS 数组来传递：
		// 因为：所有的 Redis 命令，在执行之前都会被分析，籍此来确定命令会对哪些键进行操作。因此，对于 EVAL
		// 命令来说，必须使用正确的形式来传递键，才能确保分析工作正确地执行
		System.out.println(jedisCluster.eval("return redis.call('set', KEYS[1], ARGV[1])", 1, "luaTest", "cv"));
		System.out.println(jedisCluster.get("luaTest"));

		// 注意这里需要指定KEY，因为这里lua脚本也是和slot挂钩的
		String scriptLoad = jedisCluster.scriptLoad("return redis.call('get', KEYS[1])", "luaTest");// 加载脚本
		System.out.println(scriptLoad);// 返回的SHA1校验和，后续可以直接使用这个进行操作。
		System.out.println(jedisCluster.scriptExists(scriptLoad, "luaTest"));// 检查是否存在

		System.out.println(jedisCluster.evalsha(scriptLoad, 1, "luaTest"));// 执行lua脚本

		System.out.println(jedisCluster.scriptFlush("luaTest".getBytes()));// 删除KEY as 上的所有lua脚本
		System.out.println(jedisCluster.scriptExists(scriptLoad, "luaTest"));
		// System.out.println(jedisCluster.evalsha(scriptLoad, 1, "luaTest"));//
		// 脚本已经删除，返回错误：NOSCRIPT No matching script.Please use EVAL.
	}

	/**
	 * 
	 * @Function: RedisClusterTransactionTest::testJedisCluster
	 * @Description: 测试JedisCluster
	 * @throws Exception
	 * @version: v1.0.0
	 * @author: xz
	 * @date: 2018年2月7日 上午12:45:01
	 *
	 *        Modification History: Date Author Version Description
	 *        -------------------------------------------------------------
	 */
	@Test
	public void testJedisCluster() throws Exception {
		// 大多数测试都是使用【nameKey】测试的，所以在启动之前先把这个key删掉
		jedisCluster.set("JEDIS_CLUSTER", "TEST_JEDIS_CLUSTER");
		jedisCluster.set("dd", "dd");
	}
	
	/**
	 * 
	 * @Function: RedisClusterTransactionTest::testHashTag
	 * @Description: keySlot算法中，如果key包含{}，就会使用第一个{}内部的字符串作为hash key，这样就可以保证拥有同样{}内部字符串的key就会拥有相同slot。
	 * 注意：这样的话，本来可以hash到不同的slot中的数据都放到了同一个slot中，所以使用的时候要注意数据不要太多导致一个slot数据量过大，数据分布不均匀！
	 * MSET 是一个原子性(atomic)操作，所有给定 key 都会在同一时间内被设置，某些给定 key 被更新而另一些给定 key 没有改变的情况，不可能发生。
	 * @throws InterruptedException
	 * @version: v1.0.0
	 * @author: xz
	 * @date: 2018年2月7日 上午1:49:25 
	 *
	 * Modification History:
	 * Date         Author          Version            Description
	 *-------------------------------------------------------------
	 */
	@Test
	public void testHashTag() throws InterruptedException {
		/**
		 * jedisCluster.mset("sf","d","aadf","as");
		 * 直接这样写，会报错：redis.clients.jedis.exceptions.JedisClusterException: No way to
		 * dispatch this command to Redis Cluster because keys have different slots.
		 * 这是因为key不在同一个slot中
		 */
		String prefix = "prefix";
		String KEY_SPLIT = ":"; // 用于隔开缓存前缀与缓存键值

		String result = jedisCluster.mset("{" + prefix + KEY_SPLIT + "}" + "name", "张三",
				"{" + prefix + KEY_SPLIT + "}" + "age", "23", "{" + prefix + KEY_SPLIT + "}" + "address", "adfsa",
				"{" + prefix + KEY_SPLIT + "}" + "score", "100");
		System.out.println(result);

		String name = jedisCluster.get("{" + prefix + KEY_SPLIT + "}" + "name");
		System.out.println(name);

		Long del = jedisCluster.del("{" + prefix + KEY_SPLIT + "}" + "age");
		System.out.println(del);

		List<String> values = jedisCluster.mget("{" + prefix + KEY_SPLIT + "}" + "name",
				"{" + prefix + KEY_SPLIT + "}" + "age", "{" + prefix + KEY_SPLIT + "}" + "address");
		System.out.println(values);
	}
}
