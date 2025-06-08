package me.aloic.lazybotppplus.monitor;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import me.aloic.lazybotppplus.entity.dto.osu.oauth.AccessTokenDTO;
import me.aloic.lazybotppplus.exception.LazybotRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class TokenMonitor
{
    @Value("${lazybot.client_id}")
    private Integer clientId;
    @Value("${lazybot.client_secret}")
    private String clientSecret;

    private static final Logger logger = LoggerFactory.getLogger(TokenMonitor.class);
    private static final String TOKEN_URL = "https://osu.ppy.sh/oauth/token";
    private static String token;

    @Scheduled(cron = "0 0 0/12 * * ? ")
    public void refreshClientToken()
    {
        try {
            logger.info("Getting Token for client");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("client_id", clientId);
            jsonObject.put("client_secret", clientSecret);
            jsonObject.put("grant_type", "client_credentials");
            jsonObject.put("scope", "public");
            Map<String, String > heads = new HashMap<>();
            heads.put("Accept", "application/json");
            heads.put("Content-Type", "application/json;charset=UTF-8");
            AccessTokenDTO tokenDTO = JSON.parseObject(HttpUtil.createPost(TOKEN_URL).addHeaders(heads).body(jsonObject.toString()).execute().body(),
                    AccessTokenDTO.class);
            logger.info("successfully created client token: {}", tokenDTO.getAccess_token());
            token= tokenDTO.getAccess_token();
        }
        catch (Exception e) {
            logger.error("{} : {}", e.getClass(), e.getMessage());
            throw new LazybotRuntimeException("刷新osu客户端Token失败");
        }
    }
    public static String getToken() {
        if (token == null) {
            throw new IllegalStateException("令牌尚未获取！");
        }
        return token;
    }



}
