package com.shopee.banking.bams.adapter.filter;

import com.shopee.banking.bams.common.exception.enums.AuthErrorCode;
import com.shopee.banking.bams.common.util.Asserter;
import com.shopee.banking.bams.common.util.JwtUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Objects;


@Component
public class JwtAuthFilter implements Filter {
    @Value("${bams.auth.jwt.secret}")
    private String jwtSecret;

    @Bean
    public FilterRegistrationBean<JwtAuthFilter> JwtAuthFilterRegistrationBean(JwtAuthFilter jwtAuthFilter) {
        FilterRegistrationBean<JwtAuthFilter> reg = new FilterRegistrationBean<>(jwtAuthFilter);
        reg.setUrlPatterns(List.of("/customers/*", "/admin/*"));
        reg.setOrder(1);
        return reg;
    }

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String authHeader = httpRequest.getHeader("Authorization");

        Asserter.assertTrue(authHeader != null && authHeader.startsWith("Bearer "), AuthErrorCode.INVALID_ACCESS_TOKEN);

        String token = authHeader.substring(7);
        try {
            Claims claims = JwtUtils.parseClaims(token, jwtSecret);
            Asserter.assertTrue(
                    Objects.equals(JwtUtils.ROLE_ADMIN, claims.get(JwtUtils.ROLE_CLAIM, String.class)),
                    AuthErrorCode.INVALID_ACCESS_TOKEN
            );
        } catch (JwtException | IllegalArgumentException e) {
            httpResponse.setStatus(401);
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write(String.format(
                    """
                    {
                    code: %s,
                    msg: %s
                    }
                    """,
                    AuthErrorCode.INVALID_ACCESS_TOKEN.getCode(),
                    AuthErrorCode.INVALID_ACCESS_TOKEN.getMsg()
            ));
            return;
        }
        chain.doFilter(request, response);
    }
}
