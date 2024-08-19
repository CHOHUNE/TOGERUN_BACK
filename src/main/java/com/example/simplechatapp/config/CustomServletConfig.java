package com.example.simplechatapp.config;


import com.example.simplechatapp.controller.formatter.LocalDateFormatter;
//import com.example.simplechatapp.security.resolver.CustomArgumentResolver;
import com.example.simplechatapp.controller.formatter.LocalDateTimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.datetime.standard.DateTimeFormatterRegistrar;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Configuration
@Log4j2
public class CustomServletConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {


        registry.addFormatter(new LocalDateFormatter());
        registry.addFormatter(new LocalDateTimeFormatter());



    }

//    @Bean
//    public CustomArgumentResolver customArgumentResolver() {
//        return new CustomArgumentResolver();
//    }
//
//    @Override
//    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
//        argumentResolvers.add(customArgumentResolver());
//    }

    //    @Override
//    public void addCorsMappings(CorsRegistry registry) {
//        registry.addMapping("/**")
//                .allowedOriginPatterns("http://localhost:8080", "http://localhost:3000") // use allowedOriginPatterns here
//                .allowedMethods("GET", "POST", "PUT", "DELETE")
//                .allowedHeaders("Authorization", "Content-Type")
//                .allowCredentials(true)
//                .exposedHeaders("Custom-Header")
//                .exposedHeaders("Set-Cookie")
//                .allowCredentials(true)
//                .maxAge(3600);
//    }
}
