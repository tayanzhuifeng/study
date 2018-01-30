package com.study.cache.redistemplate;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;

import com.study.cache.common.SpringContextHelper;

/**
 * Copyright: Copyright (c) 2018 Asiainfo
 * 
 * @ClassName: RedisTemplateUtil.java
 * @Description: 该类的功能描述
 *
 * @version: v1.0.0
 * @author: zangtx
 * @date: 2018年1月27日 下午10:15:54
 *
 *        Modification History: Date Author Version Description
 *        ---------------------------------------------------------* 2018年1月27日
 *        zangtx v1.0.0 修改原因
 */
public class RedisTemplateUtil1 {
	
	@SuppressWarnings("unchecked")
	private static RedisTemplate<String, Object> redisTemplate = (RedisTemplate<String, Object>) SpringContextHelper.getBean("redisTemplate");
	
	// --------------------------------------------Value为String的相关操作开始-------------------------------------------------------------------------------
	/**
	 * 
	 * @Function: RedisTemplateUtil::set
	 * @Description: value为字符串，可以传入失效时间
	 * @param key
	 * @param value
	 * @param timeout
	 * @param unit
	 * @return
	 * @version: v1.0.0
	 * @author: zangtx
	 * @date: 2018年1月27日 下午10:21:23
	 *
	 *        Modification History: Date Author Version Description
	 *        -------------------------------------------------------------
	 */
	public static boolean sset(String key, Object value, final long timeout, final TimeUnit unit) {
		try {
			redisTemplate.opsForValue().set(key, value, timeout, unit);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * 
	 * @Function: RedisTemplateUtil::sset
	 * @Description: value为字符串，不带失效时间，同一key值多次插入会覆盖之前的值
	 * @param key
	 * @param value
	 * @return
	 * @version: v1.0.0
	 * @author: zangtx
	 * @date: 2018年1月27日 下午11:52:55 
	 *
	 * Modification History:
	 * Date         Author          Version            Description
	 *-------------------------------------------------------------
	 */
	public static boolean sset(String key, Object value) {
		try {
			redisTemplate.opsForValue().set(key, value);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * 
	 * @Function: RedisTemplateUtil::sget
	 * @Description: 取出对应的value
	 * @param key
	 * @return
	 * @version: v1.0.0
	 * @author: zangtx
	 * @date: 2018年1月27日 下午11:59:46 
	 *
	 * Modification History:
	 * Date         Author          Version            Description
	 *-------------------------------------------------------------
	 */
	public static Object sget(String key) {
		return redisTemplate.opsForValue().get(key);
	}
	
	/**
	 * 
	 * @Function: RedisTemplateUtil::ssetExpireTime
	 * @Description: 
	 * @param key
	 * @param offset
	 * @param value
	 * @return
	 * @version: v1.0.0
	 * @author: zangtx
	 * @date: 2018年1月28日 下午2:47:28 
	 *
	 * Modification History:
	 * Date         Author          Version            Description
	 *-------------------------------------------------------------
	 */
	public static boolean setBit(String key, long offset, boolean value) {
		return redisTemplate.opsForValue().setBit(key, offset, value);
	}
	
	// --------------------------------------------Value为String的相关操作结束-------------------------------------------------------------------------------
	
	// --------------------------------------------Value为List的相关操作开始---------------------------------------------------------------------------------
	/**
	 * 
	 * @Function: RedisTemplateUtil::lset
	 * @Description: value为list的相关操作，index为下标
	 * @param key
	 * @param index
	 * @param value
	 * @version: v1.0.0
	 * @author: zangtx
	 * @date: 2018年1月28日 下午3:00:59 
	 *
	 * Modification History:
	 * Date         Author          Version            Description
	 *-------------------------------------------------------------
	 */
	public static void lsetByIndex(String key, long index, Object value) {
		redisTemplate.opsForList().set(key, index, value);
	}
	
	/**
	 * 
	 * @Function: RedisTemplateUtil::lleftPush
	 * @Description: 做从端压入数据
	 * @param key
	 * @param value
	 * @version: v1.0.0
	 * @author: zangtx
	 * @date: 2018年1月28日 下午3:01:34 
	 *
	 * Modification History:
	 * Date         Author          Version            Description
	 *-------------------------------------------------------------
	 */
	public static void lleftPush(String key, Object value) {
		redisTemplate.opsForList().leftPush(key, value);
	}
	
	/**
	 * 
	 * @Function: RedisTemplateUtil::lleftPushAll
	 * @Description: 一次将集合中的数据压入redis对应的key值中
	 * @param key
	 * @param values
	 * @version: v1.0.0
	 * @author: zangtx
	 * @date: 2018年1月28日 下午3:01:59 
	 *
	 * Modification History:
	 * Date         Author          Version            Description
	 *-------------------------------------------------------------
	 */
	public static void lleftPushAll(String key, List<String> values) {
		redisTemplate.opsForList().leftPushAll(key, values);
	}
	
	// --------------------------------------------Value为List的相关操作结束---------------------------------------------------------------------------------
	
}
