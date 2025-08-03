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
}
