package me.aloic.lazybotppplus.interceptor;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import me.aloic.lazybotppplus.metrics.APIMetrics;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class ApiUsageInterceptor implements HandlerInterceptor
{

    @Resource
    private APIMetrics metrics;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute("startTime", System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        long startTime = (long) request.getAttribute("startTime");
        long latency = System.currentTimeMillis() - startTime;

        String apiName = request.getRequestURI();
        boolean success = ex == null && response.getStatus() < 500;

        metrics.recordApiUsage(apiName, success, latency);
    }
}