package com.study.cache.transactions;

import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import redis.clients.jedis.Jedis;

/**
 * Copyright: Copyright (c) 2018 Asiainfo
 * 
 * @ClassName: RedisTemplateTransactionTest.java
 * @Description: 该类的功能描述
 *
 * @version: v1.0.0
 * @author: xz
 * @date: 2018年2月1日 上午10:15:27
 *
 *        Modification History: Date Author Version Description
 *        ---------------------------------------------------------* 2018年2月1日
 *        xz v1.0.0 修改原因
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:ApplicationContext.xml")
public class RedisTemplateTransactionTest {

	@Resource
	private RedisTemplate<String, Object> redisTemplate;

	@Resource
	private DefaultRedisScript<String> generateIdRedisScript;

	private InputStream input;
	private String script;

	private ThreadLocal<SimpleDateFormat> simpleDateFormatThreadLocal = new ThreadLocal<SimpleDateFormat>() {
		@Override
		public SimpleDateFormat get() {
			return new SimpleDateFormat("yyyyMMdd");
		}
	};
	
	/**
	 * 
	 * @Function: RedisTemplateTransactionTest::testRedisTemplateScript
	 * @Description: RedisCallback每次都是取一个新的connection，所以无法保证事务
	 * @throws Exception
	 * @version: v1.0.0
	 * @author: xz
	 * @date: 2018年2月6日 上午1:08:09 
	 *
	 * Modification History:
	 * Date         Author          Version            Description
	 *-------------------------------------------------------------
	 */
	@Test
	public void testRedisTemplateScript() throws Exception {
		input = new FileInputStream("src/main/resources/lua/ActiveUsersStat.lua");

		byte[] by = new byte[input.available()];
		input.read(by);

		script = new String(by);

		Object getActiveUserNum = redisTemplate.execute(new RedisCallback<Object>() {
			public Object doInRedis(RedisConnection connection) {
				Jedis jedis = (Jedis) connection.getNativeConnection();
				String luasha = jedis.scriptLoad(script);

				jedis.set("ACTIVE_USERS", "1");

				List<String> keys = new ArrayList<String>();
				List<String> args = new ArrayList<String>();

				keys.add("ACTIVE_USERS");
				args.add("6");

				return jedis.evalsha(luasha, keys, args);
			}

		}, true);

		System.out.println("在线用户：" + getActiveUserNum);
	}

	/**
	 * 
	 * @Function: RedisTemplateTransactionTest::testRedisTemplateSessionCallback
	 * @Description: 只能自己实现RedisCallBack底层，采用RedisTemplate的SesionCallback来完成在同一个Connection中，完成多个操作的方法
	 * @throws Exception
	 * @version: v1.0.0
	 * @author: xz
	 * @date: 2018年2月6日 上午1:08:56 
	 *
	 * Modification History:
	 * Date         Author          Version            Description
	 *-------------------------------------------------------------
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void testRedisTemplateSessionCallback() throws Exception {
		SessionCallback<Object> sessionCallback = new SessionCallback<Object>() {
			@SuppressWarnings("rawtypes")
			public Object execute(RedisOperations operations) throws DataAccessException {
				operations.multi();
				operations.delete("test");
				operations.opsForValue().set("test", "2");
				Object val = operations.exec();
				return val;
			}

		};
		
		System.out.println(redisTemplate.execute(sessionCallback));
	}

	/**
	 * 
	 * @Function: RedisTemplateTransactionTest::testNextIdLua
	 * @Description: 利用redis生成主键
	 * @version: v1.0.0
	 * @author: xz
	 * @date: 2018年2月6日 上午12:36:49
	 *
	 *        Modification History: Date Author Version Description
	 *        -------------------------------------------------------------
	 */
	@Test
	public void testNextIdLua() {
		String key = "TEST" + simpleDateFormatThreadLocal.get().format(new Date());
		DefaultRedisScript<String> redisScript = new DefaultRedisScript<String>();
		redisScript.setLocation(new ClassPathResource("lua/genID.lua"));
		redisScript.setResultType(String.class);
		List<String> keys = new ArrayList<String>();

		keys.add(key);

		// String nextId = redisTemplate.execute(redisScript, keys, new String());
		String nextId = redisTemplate.execute(redisScript, keys);

		System.out.println("生成的主键id：" + nextId);
	}

	/**
	 * 
	 * @Function: RedisTemplateTransactionTest::testNextIdLuaWithSpring
	 * @Description: spring-bean.xml初始化generateIdRedisScript
	 * @version: v1.0.0
	 * @author: xz
	 * @date: 2018年2月6日 上午12:38:03
	 *
	 *        Modification History: Date Author Version Description
	 *        -------------------------------------------------------------
	 */
	@Test
	public void testNextIdLuaWithSpring() {
		String key = "TEST" + simpleDateFormatThreadLocal.get().format(new Date());
		List<String> keys = new ArrayList<String>();
		keys.add(key);

		// String nextId = redisTemplate.execute(redisScript, keys, new String());
		String nextId = redisTemplate.execute(generateIdRedisScript, keys);

		System.out.println("生成的主键id：" + nextId);
	}

}
