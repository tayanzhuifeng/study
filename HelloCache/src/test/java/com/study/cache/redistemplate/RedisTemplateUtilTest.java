package com.study.cache.redistemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:ApplicationContext.xml")
public class RedisTemplateUtilTest {
	
	// --------------------------------------------------------------测试字符串的相关操作开始----------------------------------------------------------------------
	
	@Test
	public void testSetString() {
		RedisTemplateUtil.set("username_10", "xz");
		
		String value = (String)RedisTemplateUtil.get("username_10");
		
		System.out.println("testSetString value:" + value);
	}
	
	@Test
	public void testSetBit() {
		RedisTemplateUtil.set("bitTest","a");
		
        // 'a' 的ASCII码是 97。转换为二进制是：01100001
        // 'b' 的ASCII码是 98  转换为二进制是：01100010
        // 'c' 的ASCII码是 99  转换为二进制是：01100011
        //因为二进制只有0和1，在setbit中true为1，false为0，因此我要变为'b'的话第六位设置为1，第七位设置为0
		
		RedisTemplateUtil.setBit("bitTest", 6, true);
		RedisTemplateUtil.setBit("bitTest",7, false);
        System.out.println(RedisTemplateUtil.get("bitTest"));
		
	}
	
	// --------------------------------------------------------------测试字符串的相关操作开始----------------------------------------------------------------------
	
	@Test
	public void testOpsList() {
		List<Object> value = new ArrayList<Object>();
		
		value.add("listTestValue1");
		value.add("listTestValue2");
		value.add("listTestValue3");
		
		RedisTemplateUtil.lSet("listTest", value);
		
		
		List<Object> values = RedisTemplateUtil.lGet("listTest", 0, -1);
		
		for(Object tempStr : values) {
			System.out.println("testOpsList values:" + tempStr);
		}
	}
	
	// --------------------------------------------------------------测试List的相关操作开始----------------------------------------------------------------------
	
	// --------------------------------------------------------------测试HashMap的相关操作开始-------------------------------------------------------------------
	
	// redis-client通过HGETALL mapTest读取全部的key和value
	@Test
	public void testOpsHashMap() {
		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put("name", "xz");
		map.put("age", "20");
		
		RedisTemplateUtil.hmset("mapTest", map);
		
		Map<Object, Object> valueMap = RedisTemplateUtil.hmget("mapTest");
		
		for(Map.Entry<Object, Object> entry : valueMap.entrySet()) {
			System.out.println("key = " + entry.getKey() + ", Value = " + entry.getValue());
		}
		
	}
	
	// --------------------------------------------------------------测试HashMap的相关操作结束-------------------------------------------------------------------
	
	// --------------------------------------------------------------测试Set的相关操作开始-----------------------------------------------------------------------
	// smembers key 查看所有的value
	@Test
	public void testOpsSet() {
		Set<Object> set = new HashSet<Object>();
		set.add("setTest1");
		set.add("setTest2");
		
		RedisTemplateUtil.sSet("skey", set.toArray());
		
		Set<Object> otherSet = new HashSet<Object>();
		otherSet.add("setTest1");
		otherSet.add("setTest3");
		
		RedisTemplateUtil.sSet("sotherKey", otherSet.toArray());
		
		Set<Object> differenceValueSet = RedisTemplateUtil.sDifference("skey", "sotherKey");
		Set<Object> unionValueSet = RedisTemplateUtil.sUnion("skey", "sotherKey");
		Set<Object> intersectValueSet = RedisTemplateUtil.sIntersect("skey", "sotherKey");
		
		System.out.println("differenceValueSet:" + differenceValueSet.toString());
		System.out.println("unionValueSet:" + unionValueSet.toString());
		System.out.println("intersectValueSet:" + intersectValueSet.toString());
	}
	// --------------------------------------------------------------测试Set的相关操作结束-----------------------------------------------------------------------
	
	// --------------------------------------------------------------测试ZSet的相关操作开始----------------------------------------------------------------------
	@Test
	public void testOpsZSet() {
		
		RedisTemplateUtil.zsSet("zsTest", "zsTestValue3.0", 3.0);
		RedisTemplateUtil.zsSet("zsTest", "zsTestValue1.0", 1.0);
		RedisTemplateUtil.zsSet("zsTest", "zsTestValue2.0", 2.0);
		
		// redis3才支持zsscan命令，在2.x的版本会报错
		Cursor<ZSetOperations.TypedTuple<Object>> cursor = RedisTemplateUtil.zsGet("zsTest");
				
        while (cursor.hasNext()){
            ZSetOperations.TypedTuple<Object> item = cursor.next();
            System.out.println(item.getValue() + ":" + item.getScore());
        }
	}
	
	// 可将zsset用于排行榜的计算
	@Test
	public void testOpsZSetForRank() {
		RedisTemplateUtil.zsSet("Rank", "biki", 100);
		RedisTemplateUtil.zsSet("Rank", "zhibin", 87);
		RedisTemplateUtil.zsSet("Rank", "ming", 72);
		RedisTemplateUtil.zsSet("Rank", "fen", 64);
		RedisTemplateUtil.zsSet("Rank", "cat", 98);
		
		Set<TypedTuple<Object>> setValues = RedisTemplateUtil.zsRank("Rank", 0, 2);
		
		for(TypedTuple<Object> item : setValues) {
			System.out.println(item.getValue() + ":" + item.getScore());
		}
	}
	// --------------------------------------------------------------测试ZSet的相关操作结束----------------------------------------------------------------------
}
