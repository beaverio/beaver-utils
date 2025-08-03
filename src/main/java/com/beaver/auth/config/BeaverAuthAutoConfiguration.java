package com.beaver.auth.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("com.beaver.auth.filter")
public class BeaverAuthAutoConfiguration {
    // The @Component-annotated GatewaySecretFilter will be auto-discovered
}
