package com.mauersu.service.impl;

import com.mauersu.dao.RedisTemplateFactory;
import com.mauersu.util.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisConnectionUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.mauersu.dao.RedisDao;
import com.mauersu.service.RedisService;
import com.mauersu.util.RedisApplication;

import cn.workcenter.common.WorkcenterCodeEnum;
import cn.workcenter.common.WorkcenterResult;
import cn.workcenter.common.constant.WebConstant;

@Service
public class RedisServiceImpl implements RedisService  {
	
	@Autowired
	private RedisDao redisDao;
	
	@Override
	public void addRedisServer(String name, String host, int port, String password) {
		RedisApplication.createRedisConnection(name, host, password);
	}
	
	@Override
	public void addKV(String serverName, int dbIndex, String dataType,
			String key, 
			String[] values, double[] scores, String[] members, String[] fields) {
		RedisTemplate<String, Object> redisTemplate = RedisTemplateFactory.getRedisTemplate(serverName);
		redisDao.setRedisTemplate(redisTemplate);

		switch(dataType) {
		case "STRING":
			redisDao.addSTRING(serverName, dbIndex, key, values[0]);
			break;
		case "LIST":
			redisDao.addLIST(serverName, dbIndex, key, values);
			break;
		case "SET":
			redisDao.addSET(serverName, dbIndex, key, values);
			break;
		case "ZSET":
			redisDao.addZSET(serverName, dbIndex, key, scores, members);
			break;
		case "HASH":
			redisDao.addHASH(serverName, dbIndex, key, fields, values);
			break;
		}
	}
	@Override
	public WorkcenterResult getKV(String serverName, int dbIndex, String dataType, String key) {
		RedisTemplate<String, Object> redisTemplate = RedisTemplateFactory.getRedisTemplate(serverName);
		redisDao.setRedisTemplate(redisTemplate);

		Object values = null;
		switch(dataType) {
		case "STRING":
			values = redisDao.getSTRING(serverName, dbIndex, key);
			break;
		case "LIST":
			values = redisDao.getLIST(serverName, dbIndex, key);
			break;
		case "SET":
			values = redisDao.getSET(serverName, dbIndex, key);
			break;
		case "ZSET":
			values = redisDao.getZSET(serverName, dbIndex, key);
			break;
		case "HASH":
			values = redisDao.getHASH(serverName, dbIndex, key);
			break;
		case "NONE":
			//if showType = ShowTypeEnum.hide
			dataType = getDataType(serverName, dbIndex, key);
			values = getKV(serverName, dbIndex, key);
			break;
		}
		
		final String dataType1 = dataType;
		final Object values1 = values;
		return WorkcenterResult.custom().setOK(WorkcenterCodeEnum.valueOf(WebConstant.OK_REDISKV_UPDATE), new Object() {
				public String dataType;
				public Object values;
				{
					dataType = dataType1;
					values = values1;
				}
			}).build();
	}

	private String getDataType(String serverName, int dbIndex, String key) {
		RedisConnection connection = null;
		try {
			RedisTemplate redisTemplate = RedisTemplateFactory.getRedisTemplate(serverName);
			connection = RedisConnectionUtils.getConnection(redisTemplate.getConnectionFactory());
			connection.select(dbIndex);
			DataType dataType = connection.type(key.getBytes());
			return dataType.name().toUpperCase();
		}finally {
			connection.close();
		}
	}
	
	private Object getKV(String serverName, int dbIndex, String key) {
		RedisTemplate<String, Object> redisTemplate = RedisTemplateFactory.getRedisTemplate(serverName);
		redisDao.setRedisTemplate(redisTemplate);

		RedisConnection connection = null;
		try {
			connection = RedisConnectionUtils.getConnection(redisTemplate.getConnectionFactory());
			connection.select(dbIndex);
			DataType dataType = connection.type(key.getBytes());
			Object values = null;
			switch (dataType) {
				case STRING:
					values = redisDao.getSTRING(serverName, dbIndex, key);
					break;
				case LIST:
					values = redisDao.getLIST(serverName, dbIndex, key);
					break;
				case SET:
					values = redisDao.getSET(serverName, dbIndex, key);
					break;
				case ZSET:
					values = redisDao.getZSET(serverName, dbIndex, key);
					break;
				case HASH:
					values = redisDao.getHASH(serverName, dbIndex, key);
					break;
				case NONE:
					//never be here
					values = null;
			}
			return values;
		}finally {
			connection.close();
		}
	}
	@Override
	public void delKV(String serverName, int dbIndex, String deleteKeys) {
		RedisTemplate<String, Object> redisTemplate = RedisTemplateFactory.getRedisTemplate(serverName);
		redisDao.setRedisTemplate(redisTemplate);
		redisDao.delRedisKeys(serverName, dbIndex, deleteKeys);
		return;
	}
	
}
