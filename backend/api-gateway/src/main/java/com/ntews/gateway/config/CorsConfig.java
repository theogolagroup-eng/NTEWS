package com.ntews.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowCredentials(true);
        
        // Allow multiple origins for development and production
        corsConfig.setAllowedOrigins(Arrays.asList(
            "http://localhost:3000",  // React development server
            "http://localhost:3001",  // Alternative React port
            "http://localhost:8080",  // API Gateway itself
            "http://127.0.0.1:3000", // Localhost alternative
            "http://127.0.0.1:8080"  // API Gateway alternative
        ));
        
        // Allow all headers for maximum compatibility
        corsConfig.setAllowedHeaders(Arrays.asList(
            "Content-Type", 
            "Authorization", 
            "X-Requested-With",
            "Accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers",
            "X-Auth-Token",
            "X-API-Key"
        ));
        
        // Allow all necessary methods
        corsConfig.setAllowedMethods(Arrays.asList(
            "GET", 
            "POST", 
            "PUT", 
            "DELETE", 
            "OPTIONS", 
            "PATCH",
            "HEAD"
        ));
        
        // Expose headers for frontend access
        corsConfig.setExposedHeaders(Arrays.asList(
            "Content-Type",
            "Authorization",
            "X-Total-Count",
            "X-Page-Count"
        ));
        
        // Set max age for preflight requests
        corsConfig.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}
