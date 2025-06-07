package me.aloic.lazybotppplus.util;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.alibaba.fastjson2.JSON;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.aloic.lazybotppplus.enums.HTTPTypeEnum;
import me.aloic.lazybotppplus.exception.LazybotRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Component
@Data
@NoArgsConstructor
public class ApiRequestStarter
{
    private Map<String, String> headers;

    private Object body;

    private Map<String, Object> params;

    private Map<String, Object> bodies;

    private StringBuilder url;

    private static final Logger logger = LoggerFactory.getLogger(ApiRequestStarter.class);

    private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(1, 10, 30, TimeUnit.SECONDS
            , new SynchronousQueue<Runnable>(true)
            , Executors.defaultThreadFactory());

    public void setUrl(String url) {
        this.url = new StringBuilder(url);
    }

    public void setUrl(StringBuilder sb) {
        this.url = sb;
    }

    public ApiRequestStarter(String url){
        this.url = new StringBuilder(url);
    }
    public ApiRequestStarter(String url, String token){
        this.url=new StringBuilder(url);
        this.setOauth(token);
    }

    public void setDefaultHeaders(){
        //addHeader("Accept", "application/json");
        if(headers == null) {
            headers = new HashMap<>();
        }
        headers.put("Content-Type", "application/json");
        headers.put("x-api-version","20220705");
    }


    public void addHeader(String headerName, String content){
        if(headers == null)
            headers = new HashMap<>();
        setDefaultHeaders();
        headers.put(headerName, content);
    }

    public void addParams(String paramName, Object content){
        if(params == null)
            params = new HashMap<>();
        url.append(String.format("&%s=%s", paramName, content.toString()));
        params.put(paramName, content);


    }

    public void addBodies(String bodyName, Object obj){
        if(bodies == null)
            bodies = new HashMap<>();
        bodies.put(bodyName, obj);
    }


    public void setOauth(String token){
        addHeader("Authorization", String.format("Bearer %s", token));
    }

    public ApiRequestStarter withHeader(String name, String value) {
        if(headers == null) {
            headers = new HashMap<>();
        }
        headers.put(name, value);
        return this;
    }


    public <T>T executeRequest(HTTPTypeEnum type, Class<T> resultClass)
    {
        switch (type)
        {
            case HTTPTypeEnum.POST ->
            {
                HttpRequest request = HttpUtil.createPost(url.toString());
                HttpResponse response = request.addHeaders(headers).body(JSON.toJSONString(bodies)).executeAsync();
                String resp = response.body();
                T res = JSON.parseObject(resp, resultClass);
                response.close();
                logger.info("POST {} successfully with code of {}", this.getUrl(), response.getStatus());
                return res;
            }
            case HTTPTypeEnum.GET ->
            {
                int reties = 3;
                int currentAttempt = 0;
                while (currentAttempt < reties)
                {
                    try
                    {
                        HttpResponse response = HttpUtil.createGet(url.toString()).addHeaders(headers).executeAsync();
                        String resp = response.body();
                        T res = JSON.parseObject(resp, resultClass);
                        response.close();
                        logger.info("GET {} successfully with code of {}", this.getUrl(), response.getStatus());
                        return res;
                    } catch (Exception e)
                    {
                        currentAttempt++;
                        if (currentAttempt < reties)
                        {
                            try
                            {
                                TimeUnit.SECONDS.sleep(1);
                            } catch (InterruptedException ie)
                            {
                                Thread.currentThread().interrupt();
                                throw new LazybotRuntimeException("Thread interrupted while retrying", ie);
                            }
                        }
                        else
                        {
                            logger.error(e.getMessage());
                            throw new LazybotRuntimeException("HTTP请求重试" + reties + "次后仍失败:" + e.getMessage());
                        }
                    }
                }
            }
            case HTTPTypeEnum.DELETE -> HttpUtil.createRequest(Method.DELETE, url.toString()).addHeaders(headers).executeAsync().body();
        }
        return null;
    }


    public <T> List<T> executeRequestForList(HTTPTypeEnum type, Class<T> resultClass){
        switch (type)
        {
            case HTTPTypeEnum.POST ->{
                String resp = HttpUtil.createPost(url.toString()).addHeaders(headers).body(JSON.toJSONString(bodies)).execute().body();
//            System.out.println("api request resp: " + resp);
                return JSON.parseArray(resp, resultClass);
            }
            case HTTPTypeEnum.GET ->{
                int reties=3;
                int currentAttempt = 0;
                while (currentAttempt < reties) {
                    try {
                        HttpResponse response = HttpUtil.createGet(url.toString()).addHeaders(headers).execute();
//                    handleHttpCode(response.getStatus());
                        if(response.getStatus()==404) {
                            logger.warn("<list> GET {} NOT FOUND, skipping", this.getUrl());
                            return new ArrayList<>();
                        }
                        List<T> res = JSON.parseArray(response.body(), resultClass);
                        response.close();
                        logger.info("<list> GET {} successfully with code of {}", this.getUrl(), response.getStatus());
                        return res;
                    }
                    catch (Exception e) {
                        logger.warn("<list> GET {} failed", this.getUrl());
                        currentAttempt++;
                        if (currentAttempt < reties) {
                            try {
                                TimeUnit.SECONDS.sleep(1);
                            }
                            catch (InterruptedException ie) {
                                Thread.currentThread().interrupt();
                                throw new LazybotRuntimeException("Thread interrupted while retrying", ie);
                            }
                        }
                        else {
                            logger.error(e.getMessage());
                            throw new LazybotRuntimeException("HTTP请求重试" +reties +"次后仍失败:" + e.getMessage());
                        }
                    }
                }
                return null;
            }
            case HTTPTypeEnum.DELETE ->  HttpUtil.createRequest(Method.DELETE, url.toString()).addHeaders(headers).execute().body();
        }
        return new ArrayList<>();
    }

}
