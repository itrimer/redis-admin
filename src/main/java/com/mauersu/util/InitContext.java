package com.mauersu.util;

import com.mauersu.exception.RedisInitException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
@SuppressWarnings("rawtypes")
public class InitContext extends RedisApplication implements Constant  {

	private static Log log = LogFactory.getLog(InitContext.class);
	
	@Autowired
	private Environment env;
	
	@PostConstruct
	public void initRedisServers() {
		String currentServerName = "";
		try {
			int serverNum = Integer.parseInt(env.getProperty(REDISPROPERTIES_SERVER_NUM_KEY));
			for(int i=1;i<=serverNum;i++) {
				String name = env.getProperty(REDISPROPERTIES_NAME_PROFIXKEY + i);

				String host = env.getProperty(REDISPROPERTIES_HOST_PROFIXKEY + i);
				String password = env.getProperty(REDISPROPERTIES_PASSWORD_PROFIXKEY + i);
				currentServerName = host;
				createRedisConnection(name, host, password);
				
				//runUpdateLimit();
			}
		} catch (NumberFormatException e) {
			log.error("initRedisServers: " + currentServerName+" occur NumberFormatException :" + e.getMessage());
			throw new RedisInitException(e);
		} catch (Throwable e1) {
			log.error("initRedisServers: " + currentServerName+" occur Throwable :" + e1.getMessage());
			throw new RedisInitException(currentServerName + " init failed", e1);
		}
	}
	
}
