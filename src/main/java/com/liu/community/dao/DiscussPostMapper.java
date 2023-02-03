package com.liu.community.dao;

import com.liu.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper {
    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit,int orderMode);

    int selectDiscussPostRows(@Param("userId")int userId);

    int insertDiscussPost(DiscussPost discussPost);

    DiscussPost selectDiscussPostById(int id);

    int updateCommentCount(int id,int count);

//    置顶
    int updateType(int id,int type);
//    加精删除
    int updateStatus(int id,int status);
//    更新分数
    int updateScore(int id,double score);

    List<DiscussPost> selectDiscussPostsByUserId(int userId,int ignoreStatus);

    DiscussPost selectDiscussPostByIdIgnoreStatus(int id);
}
