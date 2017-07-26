package com.mauersu.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.mauersu.exception.RedisInitException;

public class RedisConfig {

	private static Log log = LogFactory.getLog(RedisConfig.class);
	
	private final static String REDIS_PROPERTIES_FILEPATH = "/redis.properties";
	
	/*@Bean(name="redisProperties")*/
	//not-in-use now
	public Properties propertiesConfig() {
		return loadProperties(REDIS_PROPERTIES_FILEPATH);
	}

	public static Properties loadProperties(String resource) {
		Properties properties = new Properties();

		InputStream is = null;
		try {
			is = openInputStream(resource);
			properties.load(is);
			return properties;
		} catch (IOException var3) {
			throw new RedisInitException("couldn't load properties file '" + resource + "' ", var3);
		} finally {
			try{is.close();}catch (Exception e) {};
		}
	}
	public static InputStream openInputStream(String resource) {
		return RedisConfig.class.getResourceAsStream(resource);
	}
}
