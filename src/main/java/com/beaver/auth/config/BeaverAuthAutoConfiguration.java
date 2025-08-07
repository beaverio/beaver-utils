package com.beaver.auth.config;

import com.beaver.auth.cookie.AuthCookieService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeaverAuthAutoConfiguration {

    @Bean
    public AuthCookieService authCookieService() {
        return new AuthCookieService();
    }
}
