package me.aloic.lazybotppplus.interceptor;

import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import me.aloic.lazybotppplus.util.JwtTokenUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Component
public class JwtFilter extends OncePerRequestFilter
{
    @Resource
    private JwtTokenUtil jwtTokenUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException
    {
        String path = request.getRequestURI();
        if (path.startsWith("/player/"))
        {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer "))
            {
                String token = authHeader.substring(7);
                try
                {
                    Map<String, Object> claims = jwtTokenUtil.tokenVerify(token);
                    request.setAttribute("clientId", claims.get("clientId"));
                } catch (Exception e)
                {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("{\"code\":401,\"msg\":\"bye bye\",\"data\":null}");
                    return;
                }
            }
            else
            {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"code\":401,\"msg\":\"bye bye\",\"data\":null}");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}