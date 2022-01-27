package com.liu.community.service;

import com.liu.community.entity.DiscussPost;
import org.springframework.stereotype.Service;

import java.util.List;


public interface DiscussPostService {
    List<DiscussPost> selectDiscussPosts(int userId,int offset,int limit);

    int selectDiscussPostRows(int userId);

    DiscussPost selectDiscussPostById(int id);

    int insertDiscussPost(DiscussPost discussPost);
}
