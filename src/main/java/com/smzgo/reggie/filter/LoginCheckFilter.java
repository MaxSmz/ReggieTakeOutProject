package com.smzgo.reggie.filter;


import com.alibaba.druid.support.json.JSONUtils;
import com.alibaba.fastjson.JSON;
import com.smzgo.reggie.common.BaseContext;
import com.smzgo.reggie.common.R;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import java.io.IOException;

@Slf4j
@WebFilter(filterName = "LoginCheckFilter",urlPatterns = "/*")
public class LoginCheckFilter implements Filter {

    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;



        // 1.获得请求的uri
        String requestURI = request.getRequestURI();

        // 定义不需要处理的请求路径
        String[] urls = new String[] {
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/common/**",
                "/user/sendMsg",
                "/user/login"
        };

        log.info("拦截到请求：{}",request.getRequestURI());

        // 2.判断本次请求是否需要处理
        boolean check = check(requestURI, urls);

        // 3.如果不需要处理则直接放行
        if (check) {
            log.info("本次请求:{}不需要处理",requestURI);
            filterChain.doFilter(request,response);
            return;
        }

        // 4-1.判断用户是否已经登陆
        if (request.getSession().getAttribute("employee") != null) {
            log.info("用户已登陆，id为:{}",request.getSession().getAttribute("employee"));

            Long empId = (Long) request.getSession().getAttribute("employee");
            BaseContext.setCurrentId(empId);

            filterChain.doFilter(request,response);
            return;
        }

        // 4-2.判断移动端用户是否已经登陆
        if (request.getSession().getAttribute("user") != null) {
            log.info("用户已登陆，id为:{}",request.getSession().getAttribute("user"));

            Long userId = (Long) request.getSession().getAttribute("user");
            BaseContext.setCurrentId(userId);

            filterChain.doFilter(request,response);
            return;
        }

        log.info("用户未登陆");
        // 5.如果未登陆则返回未登陆结果

        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return;
    }

    public boolean check(String requestURL, String[] urls) {

        for (String url : urls) {
            boolean match = PATH_MATCHER.match(url, requestURL);
            if (match) {
                return true;
            }
        }
        return false;

    }


    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
