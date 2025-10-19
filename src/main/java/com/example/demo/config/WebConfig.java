package com.example.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**") // This applies CORS to all your API endpoints
                .allowedOrigins("*") // Allows requests from any origin. For production, you might want to restrict
                                     // this to your frontend's domain.
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Allows the specified HTTP methods
                .allowedHeaders("*"); // Allows all headers
    }
}