package com.liu.community.service;

import com.liu.community.entity.Comment;

import java.util.List;

public interface CommentService {
    List<Comment> selectCommentByEntity(int entityType, int entityId, int offset, int limit);

    int selectCountByEntity(int entityType,int entityId);

    int insertComment(Comment comment);

    Comment selectCommentById(int id);

    List<Comment> selectCommentByUserId(int userId,int limit,int offset);

    int selectCommentCountByUserId(int userId);

    List<Comment> selectCommentByEntityId(int entityId);

    int updateCommentStatus(int commentId,int status);

    int updateListCommentStatus(List<Integer> commentsId,int status);
}
