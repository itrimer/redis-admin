package com.mauersu.service.impl;

import com.mauersu.dao.RedisTemplateFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.mauersu.dao.RedisDao;
import com.mauersu.service.ListService;

@Service
public class ListServiceImpl implements ListService {

	@Autowired
	private RedisDao redisDao;
	
	@Override
	public void updateListValue(String serverName, int dbIndex, String key, String value) {
		RedisTemplate redisTemplate = RedisTemplateFactory.getRedisTemplate(serverName);
		redisDao.setRedisTemplate(redisTemplate);
		redisDao.lupdateListValue(serverName, dbIndex, key, value);
	}

	@Override
	public void delListValue(String serverName, int dbIndex, String key) {
		RedisTemplate redisTemplate = RedisTemplateFactory.getRedisTemplate(serverName);
		redisDao.setRedisTemplate(redisTemplate);
		redisDao.ldelListValue(serverName, dbIndex, key);
	}

}
