package com.beaver.auth.config;

import com.beaver.auth.filter.GatewaySecretFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(name = "jakarta.servlet.Filter")
public class BeaverAuthAutoConfiguration {

    @Bean
    @ConditionalOnProperty(name = "beaver.auth.gateway-filter.enabled", havingValue = "true", matchIfMissing = true)
    public GatewaySecretFilter gatewaySecretFilter(@Value("${gateway.secret}") String gatewaySecret) {
        return new GatewaySecretFilter(gatewaySecret);
    }
}
