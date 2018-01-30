package com.study.cache.common;

import org.springframework.context.ApplicationContext;

/**
 * 
 * Copyright: Copyright (c) 2018 Asiainfo
 * 
 * @ClassName: SpringContextHelper.java
 * @Description: SpringContext工具类
 *
 * @version: v1.0.0
 * @author: zangtx
 * @date: 2018年1月27日 下午10:52:34 
 *
 * Modification History:
 * Date         Author          Version            Description
 *------------------------------------------------------------
 * 2018年1月27日     zangtx           v1.0.0               修改原因
 */
public class SpringContextHelper {
	private static ApplicationContext applicationContext;

	/**
	 * 
	 * @Function: SpringContextHelper::setApplicationContext
	 * @Description: 该函数的功能描述
	 * @param applicationContext
	 * @version: v1.0.0
	 * @author: zangtx
	 * @date: 2018年1月27日 下午10:52:45 
	 *
	 * Modification History:
	 * Date         Author          Version            Description
	 *-------------------------------------------------------------
	 */
	public static void setApplicationContext(ApplicationContext applicationContext) {
		SpringContextHelper.applicationContext = applicationContext;
	}

	/**
	 * 
	 * @Function: SpringContextHelper::getBean
	 * @Description: 该函数的功能描述
	 * @param name
	 * @return
	 * @version: v1.0.0
	 * @author: zangtx
	 * @date: 2018年1月27日 下午10:52:56 
	 *
	 * Modification History:
	 * Date         Author          Version            Description
	 *-------------------------------------------------------------
	 */
	public static Object getBean(String name) {
		return applicationContext.getBean(name);
	}

	/**
	 * 
	 * @Function: SpringContextHelper::getBean
	 * @Description: 该函数的功能描述
	 * @param clazz
	 * @return
	 * @version: v1.0.0
	 * @author: zangtx
	 * @date: 2018年1月27日 下午10:53:06 
	 *
	 * Modification History:
	 * Date         Author          Version            Description
	 *-------------------------------------------------------------
	 */
	public static <T> T getBean(Class<T> clazz) {
		return applicationContext.getBean(clazz);
	}

	/**
	 * 
	 * @Function: SpringContextHelper::containsBean
	 * @Description: 根据Bean名称判断Spring Bean对象是否存在
	 * @param name
	 * @return boolean Bean对象是否存在：true 存在；false 不存在
	 * @version: v1.0.0
	 * @author: zangtx
	 * @date: 2018年1月27日 下午10:53:13 
	 *
	 * Modification History:
	 * Date         Author          Version            Description
	 *-------------------------------------------------------------
	 */
	public static boolean containsBean(String name) {
		return applicationContext.containsBean(name);
	}

}
