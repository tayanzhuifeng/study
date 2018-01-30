package com.study.cache.redistemplate;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;

import com.study.cache.common.SpringContextHelper;

/**
 * 
 * Copyright: Copyright (c) 2018 Asiainfo
 * 
 * @ClassName: RedisTemplateUtil.java
 * @Description: 基于spring和redis的redisTemplate工具类 针对所有的hash 都是以h开头的方法 针对所有的Set
 *               都是以s开头的方法 不含通用方法 针对所有的List 都是以l开头的方法
 * @version: v1.0.0
 * @author: xz
 * @date: 2018年1月27日 下午10:11:57
 *
 *        Modification History: Date Author Version Description
 *        ------------------------------------------------------------
 *        2018年1月27日 xz v1.0.0 修改原因
 */
public class RedisTemplateUtil {

	@SuppressWarnings("unchecked")
	private static RedisTemplate<String, Object> redisTemplate = (RedisTemplate<String, Object>) SpringContextHelper
			.getBean("redisTemplate");

	// =============================common============================
	/**
	 * 
	 * @Function: RedisTemplateUtil::expire
	 * @Description: 指定缓存失效时间
	 * @param key
	 * @param time 时间(秒)
	 * @return
	 * @version: v1.0.0
	 * @author: xz
	 * @date: 2018年1月27日 下午10:13:41
	 *
	 *        Modification History: Date Author Version Description
	 *        -------------------------------------------------------------
	 */
	public static boolean expire(String key, long time) {
		try {
			if (time > 0) {
				redisTemplate.expire(key, time, TimeUnit.SECONDS);
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 
	 * @Function: CacheUtil::getExpire
	 * @Description: 获取过期时间
	 * @param key
	 * @return 时间(秒) 返回0代表为永久有效
	 * @version: v1.0.0
	 * @author: xz
	 * @date: 2018年1月28日 下午3:14:40
	 *
	 *        Modification History: Date Author Version Description
	 *        -------------------------------------------------------------
	 */
	public static long getExpire(String key) {
		return redisTemplate.getExpire(key, TimeUnit.SECONDS);
	}

	/**
	 * 
	 * @Function: CacheUtil::hasKey
	 * @Description: 判断key是否存在
	 * @param key
	 * @return true 存在 false不存在
	 * @version: v1.0.0
	 * @author: xz
	 * @date: 2018年1月28日 下午3:15:04
	 *
	 *        Modification History: Date Author Version Description
	 *        -------------------------------------------------------------
	 */
	public static boolean hasKey(String key) {
		try {
			return redisTemplate.hasKey(key);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * 
	 * @Function: RedisTemplateUtil::setBit
	 * @Description: bitmap在redis中的实现，可用于bloom filter
	 * @param key
	 * @param offset
	 * @param value
	 * @return
	 * @version: v1.0.0
	 * @author: xz
	 * @date: 2018年1月28日 下午3:47:21 
	 *
	 * Modification History:
	 * Date         Author          Version            Description
	 *-------------------------------------------------------------
	 */
	public static boolean setBit(String key, long offset, boolean value) {
		try {
			redisTemplate.opsForValue().setBit(key, offset, value);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	// ============================String=============================
	/**
	 * 
	 * @Function: CacheUtil::get
	 * @Description: 普通缓存获取
	 * @param key
	 * @return
	 * @version: v1.0.0
	 * @author: xz
	 * @date: 2018年1月28日 下午3:17:19
	 *
	 *        Modification History: Date Author Version Description
	 *        -------------------------------------------------------------
	 */
	public static Object get(String key) {
		return key == null ? null : redisTemplate.opsForValue().get(key);
	}

	/**
	 * 
	 * @Function: CacheUtil::set
	 * @Description: 普通缓存放入
	 * @param key
	 * @param value
	 * @return true成功   false失败
	 * @version: v1.0.0
	 * @author: xz
	 * @date: 2018年1月28日 下午3:18:38 
	 *
	 * Modification History:
	 * Date         Author          Version            Description
	 *-------------------------------------------------------------
	 */
	public static boolean set(String key, Object value) {
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
	 * @Function: CacheUtil::set
	 * @Description: 普通缓存放入并设置时间
	 * @param key
	 * @param value
	 * @param time 时间(秒) time要大于0 如果time小于等于0 将设置无限期
	 * @return
	 * @version: v1.0.0
	 * @author: xz
	 * @date: 2018年1月28日 下午3:19:06 
	 *
	 * Modification History:
	 * Date         Author          Version            Description
	 *-------------------------------------------------------------
	 */
	public static boolean set(String key, Object value, long time) {
		try {
			if (time > 0) {
				redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
			} else {
				set(key, value);
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 
	 * @Function: CacheUtil::incr
	 * @Description: 递增
	 * @param key
	 * @param delta  要增加几(大于0)
	 * @return
	 * @version: v1.0.0
	 * @author: xz
	 * @date: 2018年1月28日 下午3:19:44 
	 *
	 * Modification History:
	 * Date         Author          Version            Description
	 *-------------------------------------------------------------
	 */
	public static long incr(String key, long delta) {
		if (delta < 0) {
			throw new RuntimeException("递增因子必须大于0");
		}
		return redisTemplate.opsForValue().increment(key, delta);
	}

	/**
	 * 
	 * @Function: CacheUtil::decr
	 * @Description: 该函数的功能描述
	 * @param key
	 * @param delta 要减少几(小于0)
	 * @return
	 * @version: v1.0.0
	 * @author: xz
	 * @date: 2018年1月28日 下午3:20:10 
	 *
	 * Modification History:
	 * Date         Author          Version            Description
	 *-------------------------------------------------------------
	 */
	public static long decr(String key, long delta) {
		if (delta < 0) {
			throw new RuntimeException("递减因子必须大于0");
		}
		return redisTemplate.opsForValue().increment(key, -delta);
	}

	// ================================Map=================================
	/**
	 * 
	 * @Function: CacheUtil::hget
	 * @Description: HashGet
	 * @param key 键 不能为null
	 * @param item 项 不能为null
	 * @return
	 * @version: v1.0.0
	 * @author: xz
	 * @date: 2018年1月28日 下午3:20:53 
	 *
	 * Modification History:
	 * Date         Author          Version            Description
	 *-------------------------------------------------------------
	 */
	public static Object hget(String key, String item) {
		return redisTemplate.opsForHash().get(key, item);
	}

	/**
	 * 
	 * @Function: CacheUtil::hmget
	 * @Description: 获取hashKey对应的所有键值
	 * @param key
	 * @return 对应的多个键值
	 * @version: v1.0.0
	 * @author: xz
	 * @date: 2018年1月28日 下午3:28:42 
	 *
	 * Modification History:
	 * Date         Author          Version            Description
	 *-------------------------------------------------------------
	 */
	public static Map<Object, Object> hmget(String key) {
		return redisTemplate.opsForHash().entries(key);
	}

	/**
	 * 
	 * @Function: CacheUtil::hmset
	 * @Description: 该函数的功能描述
	 * @param key
	 * @param map 对应多个键值
	 * @return true 成功 false 失败
	 * @version: v1.0.0
	 * @author: xz
	 * @date: 2018年1月28日 下午3:29:04 
	 *
	 * Modification History:
	 * Date         Author          Version            Description
	 *-------------------------------------------------------------
	 */
	public static boolean hmset(String key, Map<String, Object> map) {
		try {
			redisTemplate.opsForHash().putAll(key, map);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 
	 * @Function: CacheUtil::hmset
	 * @Description: HashSet 并设置时间
	 * @param key
	 * @param map 对应多个键值
	 * @param time 时间(秒)
	 * @return true 成功 false 失败
	 * @version: v1.0.0
	 * @author: xz
	 * @date: 2018年1月28日 下午3:29:27 
	 *
	 * Modification History:
	 * Date         Author          Version            Description
	 *-------------------------------------------------------------
	 */
	public static boolean hmset(String key, Map<String, Object> map, long time) {
		try {
			redisTemplate.opsForHash().putAll(key, map);
			if (time > 0) {
				expire(key, time);
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 
	 * @Function: CacheUtil::hset
	 * @Description: 向一张hash表中放入数据,如果不存在将创建
	 * @param key
	 * @param item
	 * @param value
	 * @return true成功 false失败
	 * @version: v1.0.0
	 * @author: xz
	 * @date: 2018年1月28日 下午3:30:17 
	 *
	 * Modification History:
	 * Date         Author          Version            Description
	 *-------------------------------------------------------------
	 */
	public static boolean hset(String key, String item, Object value) {
		try {
			redisTemplate.opsForHash().put(key, item, value);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 
	 * @Function: CacheUtil::hset
	 * @Description: 向一张hash表中放入数据,如果不存在将创建
	 * @param key
	 * @param item
	 * @param value
	 * @param time 时间(秒) 注意:如果已存在的hash表有时间,这里将会替换原有的时间
	 * @return true 成功  false失败
	 * @version: v1.0.0
	 * @author: xz
	 * @date: 2018年1月28日 下午3:30:47 
	 *
	 * Modification History:
	 * Date         Author          Version            Description
	 *-------------------------------------------------------------
	 */
	public static boolean hset(String key, String item, Object value, long time) {
		try {
			redisTemplate.opsForHash().put(key, item, value);
			if (time > 0) {
				expire(key, time);
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 
	 * @Function: CacheUtil::hdel
	 * @Description: 删除hash表中的值
	 * @param key 键 不能为null
	 * @param item 项 可以使多个 不能为null
	 * @version: v1.0.0
	 * @author: xz
	 * @date: 2018年1月28日 下午3:31:13 
	 *
	 * Modification History:
	 * Date         Author          Version            Description
	 *-------------------------------------------------------------
	 */
	public static void hdel(String key, Object... item) {
		redisTemplate.opsForHash().delete(key, item);
	}

	/**
	 * 
	 * @Function: CacheUtil::hHasKey
	 * @Description: 判断hash表中是否有该项的值
	 * @param key 键 不能为null
	 * @param item 项 不能为null
	 * @return true 存在  false不存在
	 * @version: v1.0.0
	 * @author: xz
	 * @date: 2018年1月28日 下午3:31:37 
	 *
	 * Modification History:
	 * Date         Author          Version            Description
	 *-------------------------------------------------------------
	 */
	public static boolean hHasKey(String key, String item) {
		return redisTemplate.opsForHash().hasKey(key, item);
	}

	/**
	 * 
	 * @Function: CacheUtil::hincr
	 * @Description: hash递增 如果不存在,就会创建一个 并把新增后的值返回
	 * @param key
	 * @param item
	 * @param by 要增加几(大于0)
	 * @return
	 * @version: v1.0.0
	 * @author: xz
	 * @date: 2018年1月28日 下午3:32:03 
	 *
	 * Modification History:
	 * Date         Author          Version            Description
	 *-------------------------------------------------------------
	 */
	public static double hincr(String key, String item, double by) {
		return redisTemplate.opsForHash().increment(key, item, by);
	}

	/**
	 * 
	 * @Function: CacheUtil::hdecr
	 * @Description: hash递减
	 * @param key
	 * @param item
	 * @param by 要减少记(小于0)
	 * @return
	 * @version: v1.0.0
	 * @author: xz
	 * @date: 2018年1月28日 下午3:32:32 
	 *
	 * Modification History:
	 * Date         Author          Version            Description
	 *-------------------------------------------------------------
	 */
	public static double hdecr(String key, String item, double by) {
		return redisTemplate.opsForHash().increment(key, item, -by);
	}

	// ============================set=============================
	/**
	 * 
	 * @Function: CacheUtil::sGet
	 * @Description: 根据key获取Set中的所有值
	 * @param key
	 * @return
	 * @version: v1.0.0
	 * @author: xz
	 * @date: 2018年1月28日 下午3:32:50 
	 *
	 * Modification History:
	 * Date         Author          Version            Description
	 *-------------------------------------------------------------
	 */
	public static Set<Object> sGet(String key) {
		try {
			return redisTemplate.opsForSet().members(key);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 
	 * @Function: CacheUtil::sHasKey
	 * @Description: 根据value从一个set中查询,是否存在
	 * @param key
	 * @param value
	 * @return true 存在  false不存在
	 * @version: v1.0.0
	 * @author: xz
	 * @date: 2018年1月28日 下午3:33:14 
	 *
	 * Modification History:
	 * Date         Author          Version            Description
	 *-------------------------------------------------------------
	 */
	public static boolean sHasKey(String key, Object value) {
		try {
			return redisTemplate.opsForSet().isMember(key, value);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 
	 * @Function: CacheUtil::sSet
	 * @Description: 将数据放入set缓存
	 * @param key
	 * @param values 值 可以是多个
	 * @return 成功个数
	 * @version: v1.0.0
	 * @author: xz
	 * @date: 2018年1月28日 下午3:33:33 
	 *
	 * Modification History:
	 * Date         Author          Version            Description
	 *-------------------------------------------------------------
	 */
	public static long sSet(String key, Object... values) {
		try {
			return redisTemplate.opsForSet().add(key, values);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	/**
	 * 
	 * @Function: CacheUtil::sSetAndTime
	 * @Description: 将set数据放入缓存
	 * @param key
	 * @param time 时间(秒)
	 * @param values 值 可以是多个
	 * @return 成功个数
	 * @version: v1.0.0
	 * @author: xz
	 * @date: 2018年1月28日 下午3:34:35 
	 *
	 * Modification History:
	 * Date         Author          Version            Description
	 *-------------------------------------------------------------
	 */
	public static long sSetAndTime(String key, long time, Object... values) {
		try {
			Long count = redisTemplate.opsForSet().add(key, values);
			if (time > 0)
				expire(key, time);
			return count;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	/**
	 * 
	 * @Function: CacheUtil::sGetSetSize
	 * @Description: 获取set缓存的长度
	 * @param key
	 * @return
	 * @version: v1.0.0
	 * @author: xz
	 * @date: 2018年1月28日 下午3:35:08 
	 *
	 * Modification History:
	 * Date         Author          Version            Description
	 *-------------------------------------------------------------
	 */
	public static long sGetSetSize(String key) {
		try {
			return redisTemplate.opsForSet().size(key);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	/**
	 * 
	 * @Function: CacheUtil::setRemove
	 * @Description: 移除值为value的
	 * @param key
	 * @param values 值 可以是多个
	 * @return 移除的个数
	 * @version: v1.0.0
	 * @author: xz
	 * @date: 2018年1月28日 下午3:35:20 
	 *
	 * Modification History:
	 * Date         Author          Version            Description
	 *-------------------------------------------------------------
	 */
	public static long setRemove(String key, Object... values) {
		try {
			Long count = redisTemplate.opsForSet().remove(key, values);
			return count;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	/**
	 * 
	 * @Function: RedisTemplateUtil::sIntersect
	 * @Description: 获取两个key的value(Set)交集
	 * @param key
	 * @param otherKey
	 * @return
	 * @version: v1.0.0
	 * @author: xz
	 * @date: 2018年1月28日 下午4:39:06 
	 *
	 * Modification History:
	 * Date         Author          Version            Description
	 *-------------------------------------------------------------
	 */
	public static Set<Object> sIntersect(String key, String otherKey) {
		return redisTemplate.opsForSet().intersect(key, otherKey);
	}
	
	/**
	 * 
	 * @Function: RedisTemplateUtil::setDifference
	 * @Description: 计算两个key的value(Set)差集
	 * @param key
	 * @param otherKey
	 * @return
	 * @version: v1.0.0
	 * @author: xz
	 * @date: 2018年1月28日 下午4:40:55 
	 *
	 * Modification History:
	 * Date         Author          Version            Description
	 *-------------------------------------------------------------
	 */
	public static Set<Object> sDifference(String key, String otherKey) {
		return redisTemplate.opsForSet().difference(key, otherKey);
	}
	
	/**
	 * 
	 * @Function: RedisTemplateUtil::setUnion
	 * @Description: 计算两个key的value(Set)合集
	 * @param key
	 * @param otherKey
	 * @return
	 * @version: v1.0.0
	 * @author: xz
	 * @date: 2018年1月28日 下午4:43:48 
	 *
	 * Modification History:
	 * Date         Author          Version            Description
	 *-------------------------------------------------------------
	 */
	public static Set<Object> sUnion(String key, String otherKey) {
		return redisTemplate.opsForSet().union(key, otherKey);
	}
	
	// ===============================list=================================

	/**
	 * 
	 * @Function: CacheUtil::lGet
	 * @Description: 获取list缓存的内容
	 * @param key
	 * @param start 开始
	 * @param end 结束 0 到 -1代表所有值
	 * @return
	 * @version: v1.0.0
	 * @author: xz
	 * @date: 2018年1月28日 下午3:35:39 
	 *
	 * Modification History:
	 * Date         Author          Version            Description
	 *-------------------------------------------------------------
	 */
	public static List<Object> lGet(String key, long start, long end) {
		try {
			return redisTemplate.opsForList().range(key, start, end);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 
	 * @Function: CacheUtil::lGetListSize
	 * @Description: 获取list缓存的长度
	 * @param key
	 * @return
	 * @version: v1.0.0
	 * @author: xz
	 * @date: 2018年1月28日 下午3:36:00 
	 *
	 * Modification History:
	 * Date         Author          Version            Description
	 *-------------------------------------------------------------
	 */
	public static long lGetListSize(String key) {
		try {
			return redisTemplate.opsForList().size(key);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	/**
	 * 
	 * @Function: CacheUtil::lGetIndex
	 * @Description: 通过索引 获取list中的值
	 * @param key
	 * @param index 索引 index>=0时， 0 表头，1 第二个元素，依次类推；index<0时，-1，表尾，-2倒数第二个元素，依次类推
	 * @return
	 * @version: v1.0.0
	 * @author: xz
	 * @date: 2018年1月28日 下午3:36:17 
	 *
	 * Modification History:
	 * Date         Author          Version            Description
	 *-------------------------------------------------------------
	 */
	public static Object lGetIndex(String key, long index) {
		try {
			return redisTemplate.opsForList().index(key, index);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 
	 * @Function: CacheUtil::lSet
	 * @Description: 将list放入缓存
	 * @param key
	 * @param value 
	 * @return
	 * @version: v1.0.0
	 * @author: xz
	 * @date: 2018年1月28日 下午3:36:33 
	 *
	 * Modification History:
	 * Date         Author          Version            Description
	 *-------------------------------------------------------------
	 */
	public static boolean lSet(String key, Object value) {
		try {
			redisTemplate.opsForList().rightPush(key, value);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 
	 * @Function: CacheUtil::lSet
	 * @Description: 将list放入缓存
	 * @param key
	 * @param value
	 * @param time 时间(秒)
	 * @return
	 * @version: v1.0.0
	 * @author: xz
	 * @date: 2018年1月28日 下午3:36:53 
	 *
	 * Modification History:
	 * Date         Author          Version            Description
	 *-------------------------------------------------------------
	 */
	public static boolean lSet(String key, Object value, long time) {
		try {
			redisTemplate.opsForList().rightPush(key, value);
			if (time > 0)
				expire(key, time);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 
	 * @Function: CacheUtil::lSet
	 * @Description: 将list放入缓存
	 * @param key
	 * @param value
	 * @return
	 * @version: v1.0.0
	 * @author: xz
	 * @date: 2018年1月28日 下午3:37:09 
	 *
	 * Modification History:
	 * Date         Author          Version            Description
	 *-------------------------------------------------------------
	 */
	public static boolean lSet(String key, List<Object> value) {
		try {
			redisTemplate.opsForList().rightPushAll(key, value);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 
	 * @Function: CacheUtil::lSet
	 * @Description: 将list放入缓存
	 * @param key
	 * @param value
	 * @param time 时间(秒)
	 * @return
	 * @version: v1.0.0
	 * @author: xz
	 * @date: 2018年1月28日 下午3:37:28 
	 *
	 * Modification History:
	 * Date         Author          Version            Description
	 *-------------------------------------------------------------
	 */
	public static boolean lSet(String key, List<Object> value, long time) {
		try {
			redisTemplate.opsForList().rightPushAll(key, value);
			if (time > 0)
				expire(key, time);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 
	 * @Function: CacheUtil::lUpdateIndex
	 * @Description: 根据索引修改list中的某条数据
	 * @param key
	 * @param index 索引
	 * @param value 值
	 * @return
	 * @version: v1.0.0
	 * @author: xz
	 * @date: 2018年1月28日 下午3:37:43 
	 *
	 * Modification History:
	 * Date         Author          Version            Description
	 *-------------------------------------------------------------
	 */
	public static boolean lUpdateIndex(String key, long index, Object value) {
		try {
			redisTemplate.opsForList().set(key, index, value);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 
	 * @Function: CacheUtil::lRemove
	 * @Description: 移除N个值为value
	 * @param key
	 * @param count 移除多少个
	 * @param value
	 * @return 移除的个数
	 * @version: v1.0.0
	 * @author: xz
	 * @date: 2018年1月28日 下午3:38:00 
	 *
	 * Modification History:
	 * Date         Author          Version            Description
	 *-------------------------------------------------------------
	 */
	public static long lRemove(String key, long count, Object value) {
		try {
			Long remove = redisTemplate.opsForList().remove(key, count, value);
			return remove;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	// ============================zset=============================
	/**
	 * 
	 * @Function: RedisTemplateUtil::zsSet
	 * @Description: 添加value到指定的zset中
	 * @param key
	 * @param value
	 * @param score
	 * @return
	 * @version: v1.0.0
	 * @author: xz
	 * @date: 2018年1月28日 下午5:04:45 
	 *
	 * Modification History:
	 * Date         Author          Version            Description
	 *-------------------------------------------------------------
	 */
	public static boolean zsSet(String key, Object value, double score) {
		try {
			return redisTemplate.opsForZSet().add(key, value, score);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * 
	 * @Function: RedisTemplateUtil::zsGet
	 * @Description: 返回对应的value
	 * @param key
	 * @return
	 * @version: v1.0.0
	 * @author: xz
	 * @date: 2018年1月28日 下午5:10:23 
	 *
	 * Modification History:
	 * Date         Author          Version            Description
	 *-------------------------------------------------------------
	 */
	public static Cursor<ZSetOperations.TypedTuple<Object>> zsGet(String key) {
		return redisTemplate.opsForZSet().scan(key, ScanOptions.NONE);
	}
	
	/**
	 * 
	 * @Function: RedisTemplateUtil::zsRank
	 * @Description: 用于计算排名
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 * @version: v1.0.0
	 * @author: xz
	 * @date: 2018年1月28日 下午5:49:04 
	 *
	 * Modification History:
	 * Date         Author          Version            Description
	 *-------------------------------------------------------------
	 */
	public static Set<TypedTuple<Object>> zsRank(String key, long start, long end) {
		return redisTemplate.opsForZSet().reverseRangeWithScores(key, start, end);
	}
	
}
