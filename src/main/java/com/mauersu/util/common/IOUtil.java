package com.mauersu.util.common;

import com.mauersu.config.RedisConfig;
import com.mauersu.exception.RedisInitException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

/**
 * Created by zhigang.huang on 2017/9/27.
 */
public class IOUtil {

    public static void closeQuietly(InputStream s) {
        try {
            s.close();
        } catch (Exception e) {
        }
    }

    public static void closeQuietly(OutputStream s) {
        try {
            s.close();
        } catch (Exception e) {
        }
    }
}
