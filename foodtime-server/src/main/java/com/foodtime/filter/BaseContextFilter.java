package com.foodtime.filter;

import com.foodtime.context.BaseContext;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 全局请求上下文清理过滤器
 *
 *
 */
@Component
public class BaseContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } finally {
            // 无论请求成功、抛异常还是被 404 兜底，都清掉本线程的用户上下文
            BaseContext.removeCurrentId();
        }
    }
}
