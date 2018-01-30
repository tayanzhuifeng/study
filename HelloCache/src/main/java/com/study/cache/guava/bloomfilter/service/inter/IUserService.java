package com.study.cache.guava.bloomfilter.service.inter;

import java.util.List;

import com.study.cache.guava.bloomfilter.model.User;

public interface IUserService {
	List<User> getAllUser();
	
	List<User> getUserByUserName(String userName);
	
	void batchSaveUser(List<User> users);
}
