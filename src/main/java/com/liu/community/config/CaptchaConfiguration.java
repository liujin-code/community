package com.liu.community.config;

import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Configuration
public class CaptchaConfiguration{

    @Bean
    public Producer captchaProducer(){
        Properties properties = new Properties();
        DefaultKaptcha kaptcha =null;
        InputStream in = CaptchaConfiguration.class.getClassLoader().getResourceAsStream("captcha.properties");
        try {
            properties.load(in);
            kaptcha = new DefaultKaptcha();
            kaptcha.setConfig(new Config(properties));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return kaptcha;
    }
}
