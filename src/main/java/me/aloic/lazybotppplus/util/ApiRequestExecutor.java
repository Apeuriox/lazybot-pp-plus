package me.aloic.lazybotppplus.util;

import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import jakarta.annotation.Resource;
import me.aloic.lazybotppplus.enums.HTTPTypeEnum;
import me.aloic.lazybotppplus.exception.LazybotRuntimeException;
import me.aloic.lazybotppplus.monitor.TokenMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class ApiRequestExecutor
{
    @Resource
    private TokenMonitor tokenMonitor;

    private static final Logger logger = LoggerFactory.getLogger(ApiRequestExecutor.class);

    private final int MAX_RETRIES = 3;

    public <T> T execute(String url,
                         HTTPTypeEnum type,
                         String token,
                         Object body,
                         Class<T> clazz) {
        return doExecute(url, type, token, body, clazz, null);
    }

    public <T> T execute(String url,
                         HTTPTypeEnum type,
                         String token,
                         Object body,
                         TypeReference<T> typeRef) {
        return doExecute(url, type, token, body, null, typeRef);
    }

    private <T> T doExecute(String url,
                         HTTPTypeEnum type,
                         String token,
                         Object body,
                         Class<T> clazz,
                         TypeReference<T> typeRef)  {

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                HttpRequest request = createRequest(type, url, token, body);
                HttpResponse response = request.executeAsync();
                int status = response.getStatus();
                String respBody = response.body();
                if (status == 401) {
                    logger.warn("Token expired. Refreshing...");
                    tokenMonitor.refreshClientToken();
                    TimeUnit.SECONDS.sleep(10);
                    continue;
                }
                if (status >= 200 && status < 300) {
                    response.close();
                    logger.info("HTTP request successful: {}", url);
                    if (clazz != null) return JSON.parseObject(respBody, clazz);
                    else if (typeRef != null) return JSON.parseObject(respBody, typeRef.getType());
                    else throw new IllegalArgumentException("Serialization failed! must provide either Class<T> or TypeReference<T>");

                } else {
                    logger.warn("HTTP request failed: {}, status: {}, content: {}", url, status, respBody);
                    throw new LazybotRuntimeException("HTTP request failed, Status code: " + status + ", Content：" + respBody);
                }
            } catch (Exception e) {
                logger.error("Request failed  {} times: {}", attempt, e.getMessage());
                if (attempt == MAX_RETRIES) {
                    throw new LazybotRuntimeException("HTTP request failed after" + MAX_RETRIES + " retries: " + e.getMessage(), e);
                }
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    throw new LazybotRuntimeException("Request has been interrupted", interrupted);
                }
            }
        }
        return null;
    }

    private HttpRequest createRequest(HTTPTypeEnum type,
                                      String url,
                                      String token,
                                      Object bodies) {

        HttpRequest request = switch (type) {
            case GET -> HttpUtil.createGet(url);
            case POST -> HttpUtil.createPost(url).body(JSON.toJSONString(bodies));
            case DELETE -> HttpUtil.createRequest(Method.DELETE, url);
        };

        request.addHeaders(setDefaultHeaders(token));
        return request;
    }
    public Map<String, String> setDefaultHeaders(String token){
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("x-api-version","20220705");
        if (token !=null) headers.put("Authorization", String.format("Bearer %s", token));
        return headers;
    }
}
