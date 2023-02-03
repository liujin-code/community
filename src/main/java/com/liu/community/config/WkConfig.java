package com.liu.community.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.File;

@Configuration
public class WkConfig {
    private final static Logger logger = LoggerFactory.getLogger(WkConfig.class);
    @Value("${wk.images.storage}")
    private String imageStorage;
    @Value("${wk.pdfs.storage}")
    private String pdfStorage;
    @PostConstruct
    public void init(){
        File file = new File(imageStorage);
        File file1 = new File(pdfStorage);
        if (!file.exists()){
            file.mkdir();
            logger.info("创建WK图片目录: " + imageStorage);
        }
        if (!file1.exists()){
            file1.mkdir();
            logger.info("创建WK PDF目录: " + imageStorage);
        }
    }
}
