package com.liu.community.dao;

import com.liu.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentMapper {

    List<Comment> selectCommentByEntity(int entityType,int entityId,int offset,int limit);

    int selectCountByEntity(int entityType,int entityId);

    int insertComment(Comment comment);

    Comment selectCommentById(int id);

    List<Comment> selectCommentByUserId(int userId,int offset,int limit);

    int selectCommentCountById(int userId);

    List<Comment> selectCommentByEntityId(int entityId);

    int updateCommentStatus(int commentId,int status);

    int updateListCommentStatus(List<Integer> commentsId,int status);
}
