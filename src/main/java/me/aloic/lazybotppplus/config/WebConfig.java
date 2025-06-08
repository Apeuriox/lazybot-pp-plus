package me.aloic.lazybotppplus.config;

import lombok.RequiredArgsConstructor;
import me.aloic.lazybotppplus.component.RateLimitInterceptor;
import me.aloic.lazybotppplus.interceptor.IpWhitelistInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer
{

    private final IpWhitelistInterceptor ipWhitelistInterceptor;
    private final RateLimitInterceptor rateLimitInterceptor;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(ipWhitelistInterceptor)
                .addPathPatterns("/**");
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/**");

    }
}