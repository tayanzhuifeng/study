package com.study.cache.common;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

@Service
public class ApplicationContextRegister implements ApplicationContextAware{
	
	/**
	 * 
	 * @Function: ApplicationContextRegister::setApplicationContext
	 * @Description: 从上下文环境中获取applicationContext。
	 * @param applicationContext
	 * @throws BeansException
	 * @version: v1.0.0
	 * @author: zangtx
	 * @date: 2018年1月27日 下午10:55:18 
	 *
	 * Modification History:
	 * Date         Author          Version            Description
	 *-------------------------------------------------------------
	 */
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		SpringContextHelper.setApplicationContext(applicationContext);
	}

}