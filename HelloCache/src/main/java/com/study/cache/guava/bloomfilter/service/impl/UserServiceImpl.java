package com.study.cache.guava.bloomfilter.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.study.cache.guava.bloomfilter.mapper.UserMapper;
import com.study.cache.guava.bloomfilter.model.User;
import com.study.cache.guava.bloomfilter.service.inter.IUserService;

@Service
public class UserServiceImpl implements IUserService {

	@Resource
	private UserMapper userDao;
	
	public List<User> getAllUser() {
		
		return userDao.getAllUser();
	}

	public List<User> getUserByUserName(String userName) {
		
		return userDao.getUserByUserName(userName);
	}

	public void batchSaveUser(List<User> users) {
		userDao.batchSaveUser(users);
	}

}
