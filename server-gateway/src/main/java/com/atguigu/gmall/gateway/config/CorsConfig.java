package com.atguigu.gmall.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * @author Yuehong Zhang
 * @date 2021-4-12 10:29:09
 */
@Configuration // Change the current class to XXX.xml configuration class
public class CorsConfig {

    // @Bean creates a bean object and submits it to the spring container for management
    // <bean class = "org.springframework.web.cors.reactive.CorsWebFilter" />
    @Bean
    public CorsWebFilter corsWebFilter(){

        // Create CorsConfiguration
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.addAllowedHeader("*"); // any request header
        corsConfiguration.addAllowedOrigin("*"); // Allow any domain name
        corsConfiguration.addAllowedMethod("*"); // GET, POST, PUT means any
        corsConfiguration.setAllowCredentials(true);// Allow cookies

        // Need CorsConfigurationSource This object is an interface, so we need the implementation class of the current interface UrlBasedCorsConfigurationSource
        UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
        // The first parameter represents the path, and the second parameter represents information such as the way to set up cross-domain CorsConfiguration
        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**",corsConfiguration);

        // return the current object
        return new CorsWebFilter(urlBasedCorsConfigurationSource);
    }
}