package cn.workcenter.common.util;

import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class StringUtil {

    public static void main(String[] args) {
        System.out.println(getRandom(6));
    }

    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    public static String getRandom(int num) {
        String random = "";
        if (num <= 0)
            return random;
        for (int i = 0; i < num; i++) {
            random += getRandom();
        }
        return random;
    }

    public static String getRandom() {
        return String.valueOf((int) (Math.random() * 10));
    }

    public static String getString(Map<String, Object> map, String key) {
        return getString(map, key, "");
    }

    public static String getString(Map<String, Object> map, String key, String defaultValue) {
        Object obj = map.get(key);
        String value = "";
        if (obj != null){
            value = obj.toString();
        }
        return value;
    }

    public static Date addMillis(int millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MILLISECOND, millis);
        return calendar.getTime();

    }

    public static String getUUID() {
        String s = UUID.randomUUID().toString();
        // drop "-"
        return s.replaceAll("-", "");
    }

    public static String getParameterByDefault(HttpServletRequest request, String key, String defaultValue) {
        String value = request.getParameter(key);
        if (isEmpty(value)) {
            return defaultValue;
        }
        return value;
    }

}
