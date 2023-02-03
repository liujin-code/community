package com.liu.community.config;

import com.liu.community.dao.DiscussPostMapper;
import com.liu.community.dao.elasticsearch.DiscussPostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Configuration
public class elasticStartConfig {
    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private DiscussPostRepository discussPostRepository;

    @Bean
    public void init(){
        System.out.println("=========================");
        System.out.println("开始初始化selastic");
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(101, 0, 100,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(102, 0, 100,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(103, 0, 100,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(111, 0, 100,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(112, 0, 100,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(131, 0, 100,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(132, 0, 100,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(133, 0, 100,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(134, 0, 100,0));
        System.out.println("初始化完毕selastic");
    }
}
