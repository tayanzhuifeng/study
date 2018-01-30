package com.study.cache.guava.bloomfilter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.study.cache.guava.bloomfilter.model.User;
import com.study.cache.guava.bloomfilter.service.inter.IUserService;

import junit.framework.TestCase;

/**
 * 测试用户基础服务
 * @author xiangzi
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring-dao.xml", "classpath:spring-bean.xml",
		"classpath:spring-redis.xml" })
public class UserServiceTest extends TestCase {

	@Resource
	private IUserService userService;
	
	@Test
	public void testGetAllUser() {
		List<User> userList = userService.getAllUser();
		
		for(User user : userList) {
			System.out.println("用户名称：" + user.getUsername());
		}
	}

	@Test
	public void testGetUserByUserName() {
		
		String userName = "xiangzi";
		
		List<User> userList = userService.getUserByUserName(userName);
		
		for(User user : userList) {
			System.out.println("用户名称：" + user.getUsername());
		}
	}
	
	
	@Test
	public void testBatchSaveUser() {
		
		for(int i = 0; i < 50000; i++) {
			
			List<User> users = new ArrayList<User>();
			
			for(int j = 0; j < 100; j++) {
				User user = new User(UUID.randomUUID().toString(), new Random(3).nextInt(3));
				users.add(user);
			}
			
			userService.batchSaveUser(users);
		}
		
		
	}
}
