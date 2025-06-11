package me.aloic.lazybotppplus.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

@Component
public class JwtTokenUtil
{
    @Value("${lazybot.token_key}")
    private String key;
    @Value("${lazybot.expire_time}")
    private int expireTime;

    public String tokenGenerate(Map<String,Object> claims)
    {
        return JWT.create()
                .withClaim("client",claims)
                .withExpiresAt(new Date(System.currentTimeMillis() + (expireTime* 1000)))
                .sign(Algorithm.HMAC256(key));

    }

    public Map<String,Object> tokenVerify(String token)
    {
        return JWT.require(Algorithm.HMAC256(key))
                .build()
                .verify(token)
                .getClaim("client")
                .asMap();
    }
}
