package com.mauersu.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.mauersu.util.RedisApplication;

public class CleanContextFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        String contentPath = filterConfig.getServletContext().getContextPath();
        RedisApplication.BASE_PATH = contentPath;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {

        chain.doFilter(request, response);
        cleanContext();
    }

    private void cleanContext() {
        RedisApplication.setDb(0);
    }

    @Override
    public void destroy() {

    }

}
