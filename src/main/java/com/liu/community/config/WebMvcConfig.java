package com.liu.community.config;

import com.liu.community.controller.interceptor.DataInterceptor;
import com.liu.community.controller.interceptor.LoginRequiredInterceptor;
import com.liu.community.controller.interceptor.LoginTicketInterceptor;
import com.liu.community.controller.interceptor.MessageInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Component
public class WebMvcConfig implements WebMvcConfigurer {


    @Autowired
    private LoginTicketInterceptor loginTicketInterceptor;

//    @Autowired
//    private LoginRequiredInterceptor loginRequiredInterceptor;

    @Autowired
    private MessageInterceptor messageInterceptor;
    @Autowired
    private DataInterceptor dataInterceptor;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginTicketInterceptor)
                .excludePathPatterns("/**/.css","/**/.js","/**/.png", "/**/*.jpg", "/**/*.jpeg");
//        registry.addInterceptor(loginRequiredInterceptor)
//                .excludePathPatterns("/**/.css","/**/.js","/**/.png", "/**/*.jpg", "/**/*.jpeg");
        registry.addInterceptor(messageInterceptor)
                .excludePathPatterns("/**/.css","/**/.js","/**/.png", "/**/*.jpg", "/**/*.jpeg");
        registry.addInterceptor(dataInterceptor)
                .excludePathPatterns("/**/.css","/**/.js","/**/.png", "/**/*.jpg", "/**/*.jpeg");
    }
}
