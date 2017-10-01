package com.mauersu.util.common;

import com.mauersu.config.RedisConfig;
import com.mauersu.exception.RedisInitException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by zhigang.huang on 2017/9/27.
 */
public class ResourceUtil {
    public static Properties loadProperties(String resource) {
        Properties properties = new Properties();

        InputStream is = null;
        try {
            is = openInputStream(resource);
            properties.load(is);
        } catch (IOException var3) {
            throw new RedisInitException("couldn't load properties file '" + resource + "' ", var3);
        } finally {
            IOUtil.closeQuietly(is);
        }
        return properties;
    }

    public static InputStream openInputStream(String resource) {
        return ResourceUtil.class.getResourceAsStream(resource);
    }
}
