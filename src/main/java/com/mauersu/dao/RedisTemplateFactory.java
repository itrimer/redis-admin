package com.mauersu.dao;

import com.mauersu.util.Constant;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.mauersu.exception.RedisConnectionException;
import com.mauersu.util.RedisApplication;

@Service
public class RedisTemplateFactory {
    private static Log log = LogFactory.getLog(RedisTemplateFactory.class);

    public static RedisTemplate<String, Object> getRedisTemplate(String serverName) {
        RedisTemplate<String, Object> redisTemplate = Constant.redisTemplatesMap.get(serverName);
        if (redisTemplate == null) {
            log.error("redisTemplate==null" + ". had not connected to " + serverName + " this redis server now.");
            throw new RedisConnectionException("had not connected to " + serverName + " this redis server now.");
        }
        return redisTemplate;
    }

    public static void put(String serverName, RedisTemplate<String, Object> redisTemplate) {
        Constant.redisTemplatesMap.put(serverName, redisTemplate);
    }

    private static void validate(int dbIndex) {
        if (0 > dbIndex || dbIndex > 15) {
            log.error("0> dbIndex || dbIndex> 15" + "redis dbIndex is invalid : " + dbIndex);
            throw new RedisConnectionException("redis dbIndex is invalid : " + dbIndex);
        }
    }

}
