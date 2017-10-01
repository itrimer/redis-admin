package com.mauersu.util;

import com.mauersu.dao.RedisTemplateFactory;
import com.mauersu.exception.ConcurrentException;
import com.mauersu.util.redis.MyStringRedisTemplate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisConnectionUtils;
import org.springframework.data.redis.core.RedisTemplate;
import redis.clients.jedis.JedisPoolConfig;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Semaphore;

public abstract class RedisApplication {

    public static Log log = LogFactory.getLog(RedisApplication.class);

    public static volatile RefreshModeEnum refreshMode = RefreshModeEnum.manually;
    public static volatile ShowTypeEnum showType = ShowTypeEnum.show;
    public static String BASE_PATH = "/redis-admin";

    protected static volatile Semaphore limitUpdate = new Semaphore(1);
    protected static final int LIMIT_TIME = 3; //unit : second

    private static ThreadLocal<Integer> redisConnectionDbIndex = new ThreadLocal<Integer>() {
        @Override
        protected Integer initialValue() {
            return 0;
        }
    };

    protected static ThreadLocal<Semaphore> updatePermition = new ThreadLocal<Semaphore>() {
        @Override
        protected Semaphore initialValue() {
            return null;
        }
    };

    protected static ThreadLocal<Long> startTime = new ThreadLocal<Long>() {
        protected Long initialValue() {
            return 0l;
        }
    };

    private static Semaphore getSempahore() {
        startTime.set(System.currentTimeMillis());
        updatePermition.set(limitUpdate);
        return updatePermition.get();
    }

    public static boolean getUpdatePermition() {
        Semaphore sempahore = getSempahore();
        boolean permit = sempahore.tryAcquire(1);
        return permit;
    }

    public static void finishUpdate() {
        Semaphore semaphore = updatePermition.get();
        if (semaphore == null) {
            throw new ConcurrentException("semaphore==null");
        }
        final Semaphore fsemaphore = semaphore;
        new Thread(new Runnable() {
            Semaphore RSemaphore;

            {
                RSemaphore = fsemaphore;
            }

            @Override
            public void run() {
                long start = startTime.get();
                long now = System.currentTimeMillis();
                try {
                    long needWait = start + LIMIT_TIME * 1000 - now;
                    if (needWait > 0L) {
                        Thread.sleep(needWait);
                    }
                } catch (InterruptedException e) {
                    log.warn("finishUpdate 's release semaphore thread had be interrupted");
                }
                RSemaphore.release(1);
                logCurrentTime("semaphore.release(1) finish");
            }
        }).start();
    }

    //this idea is not good
    /*protected void runUpdateLimit() {
        new Thread(new Runnable () {
			@Override
			public void run() {
				while(true) {
					try {
						Thread.sleep(LIMIT_TIME * 1000);
						limitUpdate = new Semaphore(1);
					} catch(InterruptedException e) {
						log.warn("runUpdateLimit 's new semaphore thread had be interrupted");
						break;
					}
				}
			}
		}).start();
	}*/

    public static void createRedisConnection(String serverName, String host, String password) {
        RedisClusterConfiguration configuration = getRedisClusterConfiguration(host);
        JedisPoolConfig poolConfig = setRedisPoolConfig();

        JedisConnectionFactory connectionFactory = new JedisConnectionFactory(configuration);
        connectionFactory.afterPropertiesSet();
        connectionFactory.setTimeout(5);
        connectionFactory.setPoolConfig(poolConfig);

        RedisTemplate redisTemplate = new MyStringRedisTemplate();
        redisTemplate.setConnectionFactory(connectionFactory);
        redisTemplate.afterPropertiesSet();

        RedisTemplateFactory.put(serverName, redisTemplate);

        Map<String, Object> redisServerMap = new HashMap<>();
        redisServerMap.put("name", serverName);
        redisServerMap.put("host", host);
        redisServerMap.put("password", password);
        Constant.redisServerCache.add(redisServerMap);

//		initRedisKeysCache(redisTemplate, name);
//
//		RedisZtreeUtil.initRedisNavigateZtree(name);
    }

    private static JedisPoolConfig setRedisPoolConfig() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(50);
        poolConfig.setMaxIdle(5);
        poolConfig.setMinIdle(1);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);

        return poolConfig;
    }

    private static RedisClusterConfiguration getRedisClusterConfiguration(String host) {
        RedisClusterConfiguration configuration = new RedisClusterConfiguration();
        for (String nodeHost : host.split(",")) {
            String[] hostInfo = nodeHost.split(":");

            configuration.addClusterNode(new RedisNode(hostInfo[0], Integer.valueOf(hostInfo[1])));
        }
        return configuration;
    }

    private static void initRedisKeysCache(RedisTemplate redisTemplate, String name) {
        for (int i = 0; i <= Constant.REDIS_DEFAULT_DB_SIZE; i++) {
            initRedisKeysCache(redisTemplate, name, i);
        }
    }


    public static void initRedisKeysCache(RedisTemplate redisTemplate, String serverName, int dbIndex) {
        RedisConnection connection = RedisConnectionUtils.getConnection(redisTemplate.getConnectionFactory());
        connection.select(dbIndex);
        Set<byte[]> keysSet = connection.keys("*".getBytes());
        connection.close();
        List<RKey> tempList = new ArrayList<>();
        ConvertUtil.convertByteToString(connection, keysSet, tempList);
        Collections.sort(tempList);
        CopyOnWriteArrayList<RKey> redisKeysList = new CopyOnWriteArrayList<RKey>(tempList);
        if (redisKeysList.size() > 0) {
            Constant.redisKeysListMap.put(serverName + Constant.DEFAULT_SEPARATOR + dbIndex, redisKeysList);
        }
    }

    public static CopyOnWriteArrayList<RKey> iss(RedisConnection connection, String key) {
        CopyOnWriteArrayList<RKey> redisKeysList = null;
        Set<byte[]> keysSet = connection.keys(key.getBytes());
        List<RKey> tempList = new ArrayList<>();
        ConvertUtil.convertByteToString(connection, keysSet, tempList);
        Collections.sort(tempList);
        redisKeysList = new CopyOnWriteArrayList<>(tempList);
        return redisKeysList;
    }

    public static void setDb(int dbIndex) {
        RedisApplication.redisConnectionDbIndex.set(dbIndex);
    }

    public static Integer selectDb() {
        return RedisApplication.redisConnectionDbIndex.get();
    }

    public static void logCurrentTime(String code) {
        log.debug("       code:" + code + "        当前时间:" + System.currentTimeMillis());
    }
}
