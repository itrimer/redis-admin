package com.mauersu.controller;

import cn.workcenter.common.WorkcenterResult;
import cn.workcenter.common.response.WorkcenterResponseBodyJson;
import cn.workcenter.common.util.StringUtil;
import com.mauersu.service.RedisService;
import com.mauersu.service.ViewService;
import com.mauersu.util.*;
import com.mauersu.util.ztree.ZNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller
@RequestMapping("/redis")
public class RedisController {
    public static Log log = LogFactory.getLog(RedisController.class);

    @Autowired
    private ViewService viewService;
    @Autowired
    private RedisService redisService;


    @RequestMapping(method = RequestMethod.GET)
    public Object home(HttpServletRequest request, HttpServletResponse response) {
        String defaultServerName = getDefaultServerName();
        request.setAttribute("serverName", defaultServerName);
        request.setAttribute("dbIndex", Constant.DEFAULT_DBINDEX);
        return "redirect:/redis/stringList/" + defaultServerName + "/" + Constant.DEFAULT_DBINDEX;
    }

    private String getDefaultServerName() {
        Map<String, Object> serverCache = Constant.redisServerCache.get(0);
        return (String) (serverCache == null ? "" : serverCache.get("name"));
    }

    @RequestMapping(value = "/index", method = RequestMethod.GET)
    public Object index(HttpServletRequest request, HttpServletResponse response) {

        request.setAttribute("basePath", RedisApplication.BASE_PATH);
        request.setAttribute("viewPage", "home.jsp");

        String defaultServerName = getDefaultServerName();
        request.setAttribute("serverName", defaultServerName);
        request.setAttribute("dbIndex", Constant.DEFAULT_DBINDEX);
        return "admin/main";
    }

    @RequestMapping(value = "/addServer", method = RequestMethod.POST)
    @ResponseBody
    public Object addServer(HttpServletRequest request, HttpServletResponse response,
                            @RequestParam String host,
                            @RequestParam String name,
                            @RequestParam int port,
                            @RequestParam String password) {

        redisService.addRedisServer(name, host, port, password);

        return WorkcenterResponseBodyJson.custom().build();
    }

    @RequestMapping(value = "/serverTree", method = RequestMethod.GET)
    @ResponseBody
    public Object serverTree(HttpServletRequest request, HttpServletResponse response) {

        Set<ZNode> keysSet = viewService.getLeftTree();

        return keysSet;
    }

    @RequestMapping(value = "/refresh", method = RequestMethod.GET)
    @ResponseBody
    public Object refresh(HttpServletRequest request, HttpServletResponse response) {
        RedisApplication.logCurrentTime("viewService.refresh(); start");
        viewService.refresh();
        RedisApplication.logCurrentTime("viewService.refresh(); end");
        return WorkcenterResponseBodyJson.custom().build();
    }

    private void refreshByMode() {
        switch (RedisApplication.refreshMode) {
            case manually:
                break;
            case auto:
                viewService.refresh();
                break;
        }
    }

    @RequestMapping(value = "/refreshMode", method = RequestMethod.POST)
    @ResponseBody
    public Object refreshMokde(HttpServletRequest request, HttpServletResponse response,
                               @RequestParam String mode) {

        viewService.changeRefreshMode(mode);

        return WorkcenterResponseBodyJson.custom().build();
    }

    @RequestMapping(value = "/changeShowType", method = RequestMethod.POST)
    @ResponseBody
    public Object changeShowType(HttpServletRequest request, HttpServletResponse response,
                                 @RequestParam String state) {

        viewService.changeShowType(state);
        return WorkcenterResponseBodyJson.custom().build();
    }

    @RequestMapping(value = "/stringList/{serverName}/{dbIndex}", method = RequestMethod.GET)
    public Object stringList(HttpServletRequest request, HttpServletResponse response,
                             @PathVariable String serverName, @PathVariable String dbIndex) {

        refreshByMode();

        String queryMode = StringUtil.getParameterByDefault(request, "queryMode", Constant.MIDDLE_KEY);
        String queryMode_ch = QueryEnum.valueOf(queryMode).getQueryModeCh();
        String queryValue = StringUtil.getParameterByDefault(request, "queryValue", Constant.EMPTY_STRING);
        String queryByKeyPrefixs = StringUtil.getParameterByDefault(request, "queryByKeyPrefixs", Constant.EMPTY_STRING);
        String[] keyPrefixs = request.getParameterValues("keyPrefixs");
        if (keyPrefixs != null && keyPrefixs.length > 0) {
            queryByKeyPrefixs = String.join(":", keyPrefixs);
        }
        if (keyPrefixs == null || keyPrefixs.length == 0 || "".equals(keyPrefixs[0])) {
            keyPrefixs = null;
        }

        Pagination pagination = stringListPagination(request, queryMode, queryMode_ch, queryValue, queryByKeyPrefixs);

        RedisApplication.logCurrentTime("viewService.getRedisKeys start");
        Set<RKey> redisKeys = null;
        try {
            redisKeys = viewService.getRedisKeys(pagination, serverName,
                    dbIndex, keyPrefixs, queryMode, queryValue);
        } catch (Exception e) {
            log.error("", e);
        }
        RedisApplication.logCurrentTime("viewService.getRedisKeys end");
        request.setAttribute("redisServers", Constant.redisServerCache);
        request.setAttribute("basePath", RedisApplication.BASE_PATH);
        request.setAttribute("queryLabel_ch", queryMode_ch);
        request.setAttribute("queryLabel_en", queryMode);
        request.setAttribute("queryValue", queryValue);
        request.setAttribute("serverName", serverName);
        request.setAttribute("dbIndex", dbIndex);
        request.setAttribute("redisKeys", redisKeys);
        request.setAttribute("refreshMode", RedisApplication.refreshMode.getLabel());
        request.setAttribute("change2ShowType", RedisApplication.showType.getChange2());
        request.setAttribute("showType", RedisApplication.showType.getState());
        request.setAttribute("pagination", pagination.createLinkTo());
        request.setAttribute("keyPrefixs", keyPrefixs);
        request.setAttribute("viewPage", "redis/list.jsp");
        return "admin/main";
    }

    private Pagination stringListPagination(HttpServletRequest request,
                                            String queryMode, String queryMode_ch,
                                            String queryValue, String queryByKeyPrefixs) {
        Pagination pagination = getPagination(request);
        String url = "?" + "queryMode=" + queryMode + "&queryMode_ch=" + queryMode_ch
                + "&queryValue=" + queryValue + "&keyPrefixs=" + queryByKeyPrefixs;
        pagination.setLink_to(url);
        if (!StringUtil.isEmpty(queryByKeyPrefixs)) {

        }
        return pagination;
    }

    private Pagination getPagination(HttpServletRequest request) {
        String items_per_page = StringUtil.getParameterByDefault(request, "items_per_page", Constant.DEFAULT_ITEMS_PER_PAGE + "");
        String num_display_entries = StringUtil.getParameterByDefault(request, "num_display_entries", "3");
        String visit_page = StringUtil.getParameterByDefault(request, "visit_page", "0");
        String num_edge_entries = StringUtil.getParameterByDefault(request, "num_edge_entries", "2");
        String prev_text = StringUtil.getParameterByDefault(request, "prev_text", "Prev");
        String next_text = StringUtil.getParameterByDefault(request, "next_text", "Next");
        String ellipse_text = StringUtil.getParameterByDefault(request, "ellipse_text", "Next");
        String prev_show_always = StringUtil.getParameterByDefault(request, "prev_show_always", "true");
        String next_show_always = StringUtil.getParameterByDefault(request, "next_show_always", "true");

        Pagination pagination = new Pagination();
        pagination.setItems_per_page(Integer.parseInt(items_per_page));
        pagination.setNum_display_entries(Integer.parseInt(num_display_entries));
        pagination.setCurrent_page(Integer.parseInt(visit_page));
        pagination.setNum_edge_entries(Integer.parseInt(num_edge_entries));
        pagination.setPrev_text(prev_text);
        pagination.setNext_text(next_text);
        pagination.setEllipse_text(ellipse_text);
        pagination.setPrev_show_always(Boolean.parseBoolean(prev_show_always));
        pagination.setNext_show_always(Boolean.parseBoolean(next_show_always));

        return pagination;
    }

    @RequestMapping(value = "/KV", method = RequestMethod.POST)
    @ResponseBody
    public Object updateKV(HttpServletRequest request, HttpServletResponse response,
                           @RequestParam String serverName, @RequestParam int dbIndex,
                           @RequestParam String dataType,
                           @RequestParam String key) {

        String[] value = request.getParameterValues("value");
        double[] score = ConvertUtil.convert2Double(request.getParameterValues("score"));
        String[] member = request.getParameterValues("member");
        String[] field = request.getParameterValues("field");

        redisService.addKV(serverName, dbIndex, dataType, key, value, score, member, field);

        return WorkcenterResponseBodyJson.custom().build();
    }

    @RequestMapping(value = "/KV", method = RequestMethod.GET)
    @ResponseBody
    public Object getKV(HttpServletRequest request, HttpServletResponse response,
                        @RequestParam String serverName, @RequestParam int dbIndex,
                        @RequestParam String dataType,
                        @RequestParam String key) {

        WorkcenterResult result = redisService.getKV(serverName, dbIndex, dataType, key);

        return WorkcenterResponseBodyJson.custom().setAll(result, Constant.GETKV).build();
    }

    @RequestMapping(value = "/delKV", method = RequestMethod.POST)
    @ResponseBody
    public Object delKV(HttpServletRequest request, HttpServletResponse response,
                        @RequestParam String serverName, @RequestParam int dbIndex,
                        @RequestParam String deleteKeys) {

        redisService.delKV(serverName, dbIndex, deleteKeys);

        return WorkcenterResponseBodyJson.custom().build();
    }
}
