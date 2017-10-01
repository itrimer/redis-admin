package com.mauersu.config;

import com.mauersu.util.common.ResourceUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Properties;

public class RedisConfig {

	private static Log log = LogFactory.getLog(RedisConfig.class);
	
	private final static String REDIS_PROPERTIES_FILEPATH = "/redis.properties";
	
	/*@Bean(name="redisProperties")*/
	//not-in-use now
	public Properties propertiesConfig() {
		return ResourceUtil.loadProperties(REDIS_PROPERTIES_FILEPATH);
	}


}
