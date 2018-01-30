package com.study.cache.guava.bloomfilter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.base.Charsets;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import com.study.cache.guava.bloomfilter.mapper.UserMapper;
import com.study.cache.guava.bloomfilter.model.User;

/**
 * 缓存击穿
 * 首先将数据库中的数据全部放到bloom filter中，查询时先判断数据是否在bloom filter中，如果不存在，则直接返回，如果存在，则去缓存中查询，缓存中不存在，再去数据库中查询；
 * @author xiangzi
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring-dao.xml", "classpath:spring-bean.xml",
		"classpath:spring-redis.xml" })
public class CacheBreakDownTest {

	private static final int THREAD_NUM = 100;// 线程数量

	@Resource
	private UserMapper UserDao;

	// 初始化一个计数器
	private CountDownLatch countDownLatch = new CountDownLatch(THREAD_NUM);

	private BloomFilter<String> bf;

	List<User> userLists;

	@PostConstruct
	public void init() {
		// 将数据从数据库导入到本地
		userLists = UserDao.getAllUser();
		if (userLists == null || userLists.size() == 0) {
			return;
		}
		// 创建布隆过滤器(默认3%误差)
		bf = BloomFilter.create(Funnels.stringFunnel(Charsets.UTF_8), userLists.size());
		// 将数据存入布隆过滤器
		for (User user : userLists) {
			bf.put(user.getUsername());
		}
	}

	@Test
	public void cacheBreakDownTest() throws Exception {
		List<Thread> childThreadList = new ArrayList<Thread>();
		
		for (int i = 0; i < THREAD_NUM; i++) {
			Thread childThread = new Thread(new MyThread());
			childThread.start();
			
			childThreadList.add(childThread);
			// 计数器减一
			countDownLatch.countDown();
		}
		
		// 阻塞主线程，等待所有子线程运行完毕
		for(Thread childThread : childThreadList) {
			childThread.join();
		}
		
	}

	class MyThread implements Runnable {

		public void run() {
			try {
				// 所有子线程等待，当子线程全部创建完成再一起并发执行后面的代码
				countDownLatch.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			// 随机产生一个字符串
			String randomUser = UUID.randomUUID().toString();

			// 如果布隆过滤器中不存在这个用户直接返回，将流量挡掉,解决了缓存击穿的问题
			if (!bf.mightContain(randomUser)) {
				System.out.println("bloom filter don't has this user");
				return;
			}else {
				System.out.println("bloom filter has this user");
			}
			// 查询缓存，如果缓存中存在直接返回缓存数据
			/*ValueOperations<String, String> operation = (ValueOperations<String, String>) redisTemplate.opsForValue();
			
			// 为了解决缓存雪崩的问题
			synchronized (countDownLatch) {
				Object cacheUser = operation.get(key);
				if (cacheUser != null) {
					System.out.println("return user from redis");
					return;
				}
				// 如果缓存不存在查询数据库
				List<User> user = UserDao.getUserByUserName(randomUser);
				if (user == null || user.size() == 0) {
					return;
				}
				// 将mysql数据库查询到的数据写入到redis中
				System.out.println("write to redis");
				operation.set("Key:" + user.get(0).getUsername(), user.get(0).getUsername());
			}*/
		}

	}
}
