package com.example.legacy;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.UrlHandlerFilter;

@Configuration
public class WebConfig {

    @Bean
    public UrlHandlerFilter urlHandlerFilter() {
        return UrlHandlerFilter.trailingSlashHandler("/api/**").wrapRequest().build();
    }
}
