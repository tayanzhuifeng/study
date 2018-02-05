package com.study.cache.transactions;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPipeline;
import redis.clients.jedis.ShardedJedisPool;
import redis.clients.jedis.Transaction;

/**
 * Copyright: Copyright (c) 2018 Asiainfo
 * 
 * @ClassName: JedisTransactionTest.java
 * @Description: 该类的功能描述
 *
 * @version: v1.0.0
 * @author: xz
 * @date: 2018年2月1日 上午10:15:02
 *
 *        Modification History: Date Author Version Description
 *        ---------------------------------------------------------* 2018年2月1日
 *        xz v1.0.0 修改原因
 */
public class JedisTransactionTest {

	private Jedis jedis;
	private InputStream input;
	private ShardedJedisPool pool;

	@Test
	public void testJedisConnection() {
		// 实例化一个jedis对象，连接到指定的服务器，指定连接端口号
		Jedis jedis = new Jedis("127.0.0.1", 6379);
		// 将key为message的信息写入redis数据库中
		jedis.set("message", "Hello Redis!");
		// 从数据库中取出key为message的数据
		String value = jedis.get("message");
		System.out.println(value);
		// 关闭连接
		jedis.close();
	}

	/**
	 * 
	 * @Function: JedisTransactionTest::testNormalTransaction
	 * @Description:测试事务问题 其中可以用Jedis 的 watch（key）
	 *                     来实现乐观锁，有一点，如果事务处理器中出现错误，不会回滚，出错那条不会处理， 还有Jedis的 discard
	 *                     方法来取消事务，可以自己模拟多线程来看看这两个方法的使用，还有一点，
	 *                     事务是异步执行，所以不能再事务中调用get同步查询结果，都是坑呀。。。。
	 * @version: v1.0.0
	 * @author: xz
	 * @date: 2018年2月1日 上午10:30:05
	 *
	 *        Modification History: Date Author Version Description
	 *        -------------------------------------------------------------
	 */
	@SuppressWarnings("unused")
	@Test
	public void testNormalTransaction() throws Exception {
		jedis = new Jedis("127.0.0.1", 6379);
		long start = System.currentTimeMillis();

		// 有10条数据存进了redis
		for (int i = 0; i < 100000; i++) {
			if (i < 10) {
				String result = jedis.set("n" + i, "n" + i);
			} else {
				throw new Exception("Test");
			}
		}

		long end = System.currentTimeMillis();
		System.out.println("Simple SET: " + ((end - start) / 1000.0) + " seconds");
		jedis.close();
	}

	/**
	 * 
	 * @Function: JedisTransactionTest::testMultiTransaction
	 * @Description: 事务的使用
	 * @throws Exception
	 * @version: v1.0.0
	 * @author: xz
	 * @date: 2018年2月1日 下午3:06:33
	 *
	 *        Modification History: Date Author Version Description
	 *        -------------------------------------------------------------
	 */
	// multi（开启事务）+exec（提交）+discard（放弃事务）
	@Test
	public void testMultiTransaction() throws Exception {
		jedis = new Jedis("127.0.0.1", 6379);
		long start = System.currentTimeMillis();
		Transaction tx = jedis.multi();

		// 扔异常或者discard后，数据都没有存进redis中
		for (int i = 0; i < 10; i++) {
			tx.set("t" + i, "t" + i);

			if (i == 9) {
				// throw new Exception("Test");
				tx.discard();
				return;
			}
		}
		List<Object> results = tx.exec();

		System.out.println("执行结果：" + results.toString());

		long end = System.currentTimeMillis();
		System.out.println("Transaction SET: " + ((end - start) / 1000.0) + " seconds");
		jedis.close();
	}

	/**
	 * 
	 * @Function: JedisTransactionTest::testWach
	 * @Description: 线程1watch一个key值后，开启事务，执行命令，如果当前key被其他线程修改，则线程1放弃当前事务执行
	 *               通过watch实际上类似于数据库层面事务和乐观锁的关系
	 * @version: v1.0.0
	 * @author: xz
	 * @date: 2018年2月1日 下午3:06:16
	 *
	 *        Modification History: Date Author Version Description
	 *        -------------------------------------------------------------
	 */
	// multi（开启事务）+exec（提交）+discard（放弃事务）+watch(CAS乐观锁)
	@Test
	public void testWach() {
		jedis = new Jedis("127.0.0.1", 6379);
		jedis.watch("testabcd");

		Transaction multi = jedis.multi();
		multi.set("testabcd", "23432");

		// 更新被watch的值，这样当前线程放弃事务的执行，从multi.exec()的执行结果可以看出
		updateWatchedValue();

		List<Object> exec = multi.exec();
		System.out.println("---" + exec);

		jedis.unwatch();
		jedis.close();
	}

	@Test
	public void updateWatchedValue() {
		Jedis jedis1 = new Jedis("127.0.0.1", 6379);

		jedis1.set("testabcd", "125");

		jedis1.close();
	}

	/**
	 * 
	 * @Function: JedisTransactionTest::testPipelined
	 * @Description: 管道技术可以在服务端未响应时，客户端可以继续向服务端发送请求，并最终一次性读取所有服务端的响应。 管道技术最显著的优势是提高了
	 *               redis 服务的性能。
	 * @version: v1.0.0
	 * @author: xz
	 * @date: 2018年2月1日 下午3:32:58
	 *
	 *        Modification History: Date Author Version Description
	 *        -------------------------------------------------------------
	 */
	@Test
	public void testPipelined() {
		jedis = new Jedis("127.0.0.1", 6379);
		Pipeline pipeline = jedis.pipelined();

		long start = System.currentTimeMillis();

		for (int i = 0; i < 100000; i++) {
			pipeline.set("p" + i, "p" + i);
		}

		pipeline.syncAndReturnAll();

		long end = System.currentTimeMillis();

		System.out.println("Pipelined SET: " + ((end - start) / 1000.0) + " seconds");

		jedis.close();
	}

	/**
	 * 
	 * @Function: JedisTransactionTest::testPipelinedWithTransaction
	 * @Description: 管道中增加事务
	 * @throws Exception
	 * @version: v1.0.0
	 * @author: xz
	 * @date: 2018年2月1日 下午3:44:48
	 *
	 *        Modification History: Date Author Version Description
	 *        -------------------------------------------------------------
	 */
	@Test
	public void testPipelinedWithTransaction() throws Exception {
		jedis = new Jedis("127.0.0.1", 6379);
		long start = System.currentTimeMillis();

		Pipeline pipeline = jedis.pipelined();
		pipeline.multi();

		for (int i = 0; i < 100000; i++) {
			pipeline.set("" + i, "" + i);

			/*
			 * if(i == 99999) { throw new Exception("Test"); }
			 */
		}

		pipeline.exec();

		 pipeline.syncAndReturnAll();
		long end = System.currentTimeMillis();
		System.out.println("Pipelined transaction: " + ((end - start) / 1000.0) + " seconds");
		jedis.close();
	}

	/**
	 * 
	 * @Function: JedisTransactionTest::testShardNormal
	 * @Description: 分布式直接连接，并且是同步调用，每步执行都返回执行结果。
	 * @version: v1.0.0
	 * @author: xz
	 * @date: 2018年2月1日 下午4:43:08
	 *
	 *        Modification History: Date Author Version Description
	 *        -------------------------------------------------------------
	 */
	@Test
	public void testShardNormal() {
		List<JedisShardInfo> shards = Arrays.asList(new JedisShardInfo("127.0.0.1", 6379),
				new JedisShardInfo("127.0.0.1", 6380));

		ShardedJedis sharding = new ShardedJedis(shards);

		long start = System.currentTimeMillis();
		for (int i = 0; i < 100000; i++) {
			sharding.set("sn" + i, "n" + i);
		}
		long end = System.currentTimeMillis();
		System.out.println("Simple@Sharing SET: " + ((end - start) / 1000.0) + " seconds");

		sharding.close();
	}

	/**
	 * 
	 * @Function: JedisTransactionTest::testShardPipelined
	 * @Description: 该函数的功能描述
	 * @version: v1.0.0
	 * @author: xz
	 * @date: 2018年2月1日 下午9:49:43
	 *
	 *        Modification History: Date Author Version Description
	 *        -------------------------------------------------------------
	 */
	@Test
	public void testShardPipelined() {
		List<JedisShardInfo> shards = Arrays.asList(new JedisShardInfo("127.0.0.1", 6379),
				new JedisShardInfo("127.0.0.1", 6380));

		ShardedJedis sharding = new ShardedJedis(shards);

		ShardedJedisPipeline pipeline = sharding.pipelined();
		long start = System.currentTimeMillis();
		for (int i = 0; i < 100000; i++) {
			pipeline.set("sp" + i, "p" + i);
		}
		pipeline.syncAndReturnAll();
		long end = System.currentTimeMillis();
		System.out.println("Pipelined@Sharing SET: " + ((end - start) / 1000.0) + " seconds");

		sharding.close();
	}

	// 多线程方式，直接连接不安全，需要使用连接池方式，分布式连接池同步调用;分布式环境下ShardedJedis不支持事务
	/**
	 * 
	 * @Function: JedisTransactionTest::testShardSimplePool
	 * @Description: 分布式连接池同步调用
	 * @version: v1.0.0
	 * @author: xz
	 * @date: 2018年2月1日 下午9:50:33
	 *
	 *        Modification History: Date Author Version Description
	 *        -------------------------------------------------------------
	 */
	@Test
	public void testShardSimplePool() {
		List<JedisShardInfo> shards = Arrays.asList(new JedisShardInfo("127.0.0.1", 6379),
				new JedisShardInfo("127.0.0.1", 6380));

		pool = new ShardedJedisPool(new JedisPoolConfig(), shards);

		ShardedJedis one = pool.getResource();

		long start = System.currentTimeMillis();
		for (int i = 0; i < 100000; i++) {
			one.set("spn" + i, "n" + i);
		}
		long end = System.currentTimeMillis();
		one.close();
		System.out.println("Simple@Pool SET: " + ((end - start) / 1000.0) + " seconds");

		pool.destroy();
	}

	/**
	 * 
	 * @Function: JedisTransactionTest::testShardPipelinedPool
	 * @Description: 分布式连接池异步调用
	 * @version: v1.0.0
	 * @author: xz
	 * @date: 2018年2月1日 下午9:50:46
	 *
	 *        Modification History: Date Author Version Description
	 *        -------------------------------------------------------------
	 */
	@Test
	public void testShardPipelinedPool() {
		List<JedisShardInfo> shards = Arrays.asList(new JedisShardInfo("127.0.0.1", 6379),
				new JedisShardInfo("127.0.0.1", 6380));

		pool = new ShardedJedisPool(new JedisPoolConfig(), shards);

		ShardedJedis one = pool.getResource();

		ShardedJedisPipeline pipeline = one.pipelined();

		long start = System.currentTimeMillis();
		for (int i = 0; i < 100000; i++) {
			pipeline.set("sppn" + i, "n" + i);
		}
		List<Object> results = pipeline.syncAndReturnAll();
		
		System.out.println(results);
		
		long end = System.currentTimeMillis();
		one.close();
		System.out.println("Pipelined@Pool SET: " + ((end - start) / 1000.0) + " seconds");
		pool.destroy();
	}

	// lua脚本
	@Test
	public void testLuaScript() throws Exception {
		jedis = new Jedis("127.0.0.1", 6379);

		input = new FileInputStream("src/main/resources/lua/Test.lua");

		byte[] by = new byte[input.available()];
		input.read(by);

		String script = new String(by);

		jedis.eval(script);
	}
	
	// ----------------------------------redis lua开始-----------------------------------------------------------------------
	
	/**
	 * 
	 * @Function: JedisTransactionTest::testLoadLua
	 * @Description: 活跃用户判断：判断一个游戏用户是否属于活跃用户，如果符合标准，则活跃用户人数+1
	 * @throws Exception
	 * @version: v1.0.0
	 * @author: xz
	 * @date: 2018年2月5日 下午5:25:56 
	 *
	 * Modification History:
	 * Date         Author          Version            Description
	 *-------------------------------------------------------------
	 */
	@Test
	public void testActiveUsersStat() throws Exception {
		jedis = new Jedis("127.0.0.1", 6379);
		
		input = new FileInputStream("src/main/resources/lua/ActiveUsersStat.lua");

		byte[] by = new byte[input.available()];
		input.read(by);

		String script = new String(by);
		
		String luasha = jedis.scriptLoad(script);
		
		List<String> keys = new ArrayList<String>();
		List<String> args = new ArrayList<String>();
		
		jedis.set("ACTIVE_USERS", "1");
		
		keys.add("ACTIVE_USERS");
		args.add("4");
		
		jedis.evalsha(luasha, keys, args);
	}
	
	/**
	 * 
	 * @Function: JedisTransactionTest::testRateLimiter
	 * @Description: 测试限流，简单DDOS防护：限制n秒内同ip的访问次数
	 * @throws Exception
	 * @version: v1.0.0
	 * @author: xz
	 * @date: 2018年2月5日 下午5:26:48 
	 *
	 * Modification History:
	 * Date         Author          Version            Description
	 *-------------------------------------------------------------
	 */
	@Test
	public void testRateLimiter() throws Exception {
		jedis = new Jedis("127.0.0.1", 6379);
		
		input = new FileInputStream("src/main/resources/lua/RateLimiter.lua");

		byte[] by = new byte[input.available()];
		input.read(by);

		String script = new String(by);
		
		List<String> keys = new ArrayList<String>();
		List<String> args = new ArrayList<String>();
		
		keys.add("requestNumKey");
		args.add("10");
		args.add("1000");
		
		jedis.eval(script, keys, args);
	}
	
	/**
	 * 
	 * @Function: JedisTransactionTest::testGetGoods
	 * @Description: 获取游戏商店中的货品：取出hash表中符合条件的对象
	 * @throws Exception
	 * @version: v1.0.0
	 * @author: xz
	 * @date: 2018年2月5日 下午9:04:52 
	 *
	 * Modification History:
	 * Date         Author          Version            Description
	 *-------------------------------------------------------------
	 */
	@Test
	public void testGetGoods() throws Exception {
		jedis = new Jedis("127.0.0.1", 6379);
		
		// ---------------------------------初始化数据开始----------------------------------------
		jedis.sadd("produce", "apples", "oranges", "broccoli");
		
		Map<String, String> map = new HashMap<String, String>();
		
		map.put("bread", "2");
		map.put("apples", "5");
		map.put("oranges", "6");
		map.put("broccoli", "1");
		
		jedis.hmset("groceries", map);
		
		// ---------------------------------初始化数据结束----------------------------------------
		
		input = new FileInputStream("src/main/resources/lua/GetGoods.lua");

		byte[] by = new byte[input.available()];
		input.read(by);

		String script = new String(by);
		
		String luasha = jedis.scriptLoad(script);
		
		List<String> keys = new ArrayList<String>();
		List<String> args = new ArrayList<String>();
		
		keys.add("groceries");
		keys.add("produce");
		
		Object goodsInfo = jedis.evalsha(luasha, keys, args);
		
		System.err.println(goodsInfo.toString());
	}
	
	/**
	 * 
	 * @Function: JedisTransactionTest::getAverageValue
	 * @Description: 数据分析：通过Lua脚本实现数据格式化,实时平均值统计
	 * @throws Exception
	 * @version: v1.0.0
	 * @author: xz
	 * @date: 2018年2月5日 下午9:43:18 
	 *
	 * Modification History:
	 * Date         Author          Version            Description
	 *-------------------------------------------------------------
	 */
	@Test
	public void getAverageValue() throws Exception {
		jedis = new Jedis("127.0.0.1", 6379);
		
		input = new FileInputStream("src/main/resources/lua/GetAverageValue.lua");

		byte[] by = new byte[input.available()];
		input.read(by);

		String script = new String(by);
		
		String luasha = jedis.scriptLoad(script);
		
		List<String> keys = new ArrayList<String>();
		List<String> args = new ArrayList<String>();
		
		
		keys.add("score:avg");
		keys.add("score:count");
		
		args.add("80");
		
		jedis.del("score:avg");
		jedis.del("score:count");
		
		jedis.evalsha(luasha, keys, new ArrayList<String>(Arrays.asList("80")));
		jedis.evalsha(luasha, keys, new ArrayList<String>(Arrays.asList("100")));
		jedis.evalsha(luasha, keys, new ArrayList<String>(Arrays.asList("75")));
		jedis.evalsha(luasha, keys, new ArrayList<String>(Arrays.asList("98")));
		jedis.evalsha(luasha, keys, new ArrayList<String>(Arrays.asList("98")));
		Object averageValue = jedis.evalsha(luasha, keys, new ArrayList<String>(Arrays.asList("98")));
		
		System.out.println("averageValue:" + averageValue);
	}
	
	
	// ----------------------------------redis lua结束-----------------------------------------------------------------------
	
}
