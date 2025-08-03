package com.beaver.auth.config;

import com.beaver.auth.cookie.AuthCookieService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeaverAuthAutoConfiguration {

    @Bean
    public AuthCookieService authCookieService() {
        return new AuthCookieService();
    }

    // Servlet-specific beans - using string class names to avoid loading servlet classes in reactive environments
    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @ConditionalOnClass(name = "jakarta.servlet.http.HttpServletRequest")
    public Object servletTokenExtractor() {
        try {
            Class<?> clazz = Class.forName("com.beaver.auth.cookie.ServletTokenExtractor");
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create ServletTokenExtractor", e);
        }
    }

    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @ConditionalOnClass(name = "jakarta.servlet.Filter")
    @ConditionalOnProperty(name = "beaver.auth.gateway-filter.enabled", havingValue = "true", matchIfMissing = true)
    public Object gatewaySecretFilter(@Value("${gateway.secret}") String gatewaySecret) {
        try {
            Class<?> clazz = Class.forName("com.beaver.auth.filter.GatewaySecretFilter");
            return clazz.getDeclaredConstructor(String.class).newInstance(gatewaySecret);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create GatewaySecretFilter", e);
        }
    }

    // Reactive-specific beans
    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    @ConditionalOnClass(name = "org.springframework.http.server.reactive.ServerHttpRequest")
    public Object reactiveTokenExtractor() {
        try {
            Class<?> clazz = Class.forName("com.beaver.auth.cookie.ReactiveTokenExtractor");
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create ReactiveTokenExtractor", e);
        }
    }
}
