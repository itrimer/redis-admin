package com.mauersu.service.impl;

import cn.workcenter.common.util.StringUtil;
import com.mauersu.dao.RedisTemplateFactory;
import com.mauersu.service.ViewService;
import com.mauersu.util.*;
import com.mauersu.util.ztree.RedisZtreeUtil;
import com.mauersu.util.ztree.ZNode;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisConnectionUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.NumberUtils;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
public class ViewServiceImpl implements ViewService {

    @Override
    public void changeRefreshMode(String mode) {
        RedisApplication.refreshMode = RefreshModeEnum.valueOf(mode);
    }

    @Override
    public Set<ZNode> getLeftTree() {
        return getLeftTree(RedisApplication.refreshMode);
    }

    private Set<ZNode> getLeftTree(RefreshModeEnum refreshMode) {
        switch (refreshMode) {
            case manually:
                break;
            case auto:
                for (Map<String, Object> redisServerMap : Constant.redisServerCache) {
                    RedisZtreeUtil.refreshRedisNavigateZtree((String) redisServerMap.get("name"));
                }
                break;
        }
        return new TreeSet<ZNode>(Constant.redisNavigateZtree);

    }

    @Override
    public Set<ZNode> refresh() {
        boolean permit = RedisApplication.getUpdatePermition();
        Set<ZNode> zTree = null;
        if (permit) {
            try {
                RedisApplication.logCurrentTime("try {");
                for (Map<String, Object> redisServerMap : Constant.redisServerCache) {
                    RedisApplication.logCurrentTime("refreshKeys(" + (String) redisServerMap.get("name"));
//					for (int i = 0; i <= REDIS_DEFAULT_DB_SIZE; i++) {
//						refreshKeys((String) redisServerMap.get("name"), i);
//					}
                    RedisApplication.logCurrentTime("refreshServerTree(" + (String) redisServerMap.get("name"));
                    zTree = refreshServerTree((String) redisServerMap.get("name"), Constant.DEFAULT_DBINDEX);
                    // test limit flow System.out.println("yes permit");
                    RedisApplication.logCurrentTime("continue");
                }
                RedisApplication.logCurrentTime("finally {");
            } finally {
                RedisApplication.finishUpdate();
            }
        } else {
            // test limit flow System.out.println("no permit");
        }
        return zTree;
    }

    @Override
    public void refreshAllKeys() {
        boolean permit = RedisApplication.getUpdatePermition();
        try {
            for (Map<String, Object> redisServerMap : Constant.redisServerCache) {
                for (int i = 0; i <= Constant.REDIS_DEFAULT_DB_SIZE; i++) {
                    refreshKeys((String) redisServerMap.get("name"), i);
                }
            }
        } finally {
            RedisApplication.finishUpdate();
        }
    }

    private void refreshKeys(String serverName, int dbIndex) {
        RedisTemplate redisTemplate = RedisTemplateFactory.getRedisTemplate(serverName);
        RedisApplication.initRedisKeysCache(redisTemplate, serverName, dbIndex);
    }

    private Set<ZNode> refreshServerTree(String serverName,
                                         int dbIndex) {
        RedisZtreeUtil.refreshRedisNavigateZtree(serverName);
        return new TreeSet<ZNode>(Constant.redisNavigateZtree);
    }

    @Override
    public Set<RKey> getRedisKeys(Pagination pagination, String serverName, String dbIndex, String[] keyPrefixs, String queryMode, String queryValue) {
        Integer iDbIndex = NumberUtils.parseNumber(dbIndex, Integer.class);

        List<RKey> allRedisKeys = null;
        if (!StringUtil.isEmpty(queryValue)) {
            RedisTemplate redisTemplate = RedisTemplateFactory.getRedisTemplate(serverName);
            RedisConnection connection = RedisConnectionUtils.getConnection(redisTemplate.getConnectionFactory());
            connection.select(iDbIndex);
            try {
                allRedisKeys = RedisApplication.iss(connection, "*" + queryValue + "*");
            }finally {
                connection.close();
            }
        }

//		List<RKey> allRedisKeys = redisKeysListMap.get(serverName + DEFAULT_SEPARATOR + dbIndex);

        Set<RKey> resultRedisKeys = null;

        if (allRedisKeys == null || allRedisKeys.size() == 0) {
            pagination.setMaxentries(0);
            resultRedisKeys = new TreeSet<>();
            return resultRedisKeys;
        }

        if (keyPrefixs == null || keyPrefixs.length == 0) {
            RedisApplication.logCurrentTime("keyPrefixs == null");
            if (StringUtils.isEmpty(queryValue)) {
                RedisApplication.logCurrentTime("new TreeSet<RKey>(allRedisKeys);");
                int toIndex = pagination.getToIndex() > allRedisKeys.size() ? allRedisKeys.size() : pagination.getToIndex();
                List<RKey> resultList = allRedisKeys.subList(pagination.getFromIndex(), toIndex);
                resultRedisKeys = new TreeSet<RKey>(resultList);
                pagination.setMaxentries(allRedisKeys.size());
            } else {
                List<RKey> queryRedisKeys = getQueryRedisKeys(allRedisKeys, queryMode, queryValue);
                Collections.sort(queryRedisKeys);//arraylist sort
                int toIndex = pagination.getToIndex() > queryRedisKeys.size() ? queryRedisKeys.size() : pagination.getToIndex();
                List<RKey> resultList = queryRedisKeys.subList(pagination.getFromIndex(), toIndex);
                resultRedisKeys = new TreeSet<RKey>(resultList);
                pagination.setMaxentries(queryRedisKeys.size());
            }
        } else {
            StringBuffer keyPrefix = new StringBuffer("");
            for (String prefix : keyPrefixs) {
                keyPrefix.append(prefix).append(Constant.DEFAULT_REDISKEY_SEPARATOR);
            }
            List<RKey> conformRedisKeys = getConformRedisKeys(allRedisKeys, keyPrefix.toString());
            Collections.sort(conformRedisKeys);//arraylist sort
            int toIndex = pagination.getToIndex() > conformRedisKeys.size() ? conformRedisKeys.size() : pagination.getToIndex();
            List<RKey> resultList = conformRedisKeys.subList(pagination.getFromIndex(), toIndex);
            resultRedisKeys = new TreeSet<RKey>(resultList);
            pagination.setMaxentries(conformRedisKeys.size());
        }
        return resultRedisKeys;
    }

    private List<RKey> getQueryRedisKeys(List<RKey> allRedisKeys, String queryMode, String queryValue) {
        List<RKey> rKeySet = new ArrayList<RKey>();
        for (RKey rKey : allRedisKeys) {
            switch (queryMode) {
                case Constant.MIDDLE_KEY:
                    if (rKey.contains(queryValue)) {
                        rKeySet.add(rKey);
                    }
                    break;
                case Constant.HEAD_KEY:
                    if (rKey.startsWith(queryValue)) {
                        rKeySet.add(rKey);
                    }
                    break;
                case Constant.TAIL_KEY:
                    if (rKey.endsWith(queryValue)) {
                        rKeySet.add(rKey);
                    }
                    break;
            }
        }
        return rKeySet;
    }

    private List<RKey> getConformRedisKeys(List<RKey> allRedisKeys, String keyPrefix) {
        List<RKey> rKeySet = new ArrayList<RKey>();
        for (RKey rKey : allRedisKeys) {
            if (rKey.startsWith(keyPrefix)) {
                rKeySet.add(rKey);
            }
        }
        return rKeySet;
    }

    @Override
    public void changeShowType(String state) {
        RedisApplication.showType = ShowTypeEnum.valueOf(state);
        switch (RedisApplication.showType) {
            case show:
                //get redisKeys again if init keys with ShowTypeEnum.hide
                refreshAllKeys();
                break;
            case hide:
                break;
        }
    }

}
