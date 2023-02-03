package com.liu.community.service;

import com.liu.community.entity.DiscussPost;
import org.springframework.stereotype.Service;

import java.util.List;


public interface DiscussPostService {
    List<DiscussPost> selectDiscussPosts(int userId,int offset,int limit,int orderMode);

    int selectDiscussPostRows(int userId);

    DiscussPost selectDiscussPostById(int id);

    int insertDiscussPost(DiscussPost discussPost);
    //    置顶
    int updateType(int id,int type);
    //    加精删除
    int updateStatus(int id,int status);

    int updateScore(int id,double score);

    List<DiscussPost> selectDiscussPostsByUserId(int id,int ignoreStatus);

    DiscussPost selectDiscussPostByIdIgnoreStatus(int id);
}
