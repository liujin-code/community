package com.liu.community.service.Impl;

import com.liu.community.dao.CommentMapper;
import com.liu.community.dao.DiscussPostMapper;
import com.liu.community.entity.Comment;
import com.liu.community.service.CommentService;
import com.liu.community.utils.CommunityConstant;
import com.liu.community.utils.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class CommentServiceImpl implements CommentService, CommunityConstant {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Override
    public List<Comment> selectCommentByEntity(int entityType, int entityId, int offset, int limit) {
        List<Comment> comments = commentMapper.selectCommentByEntity(entityType, entityId, offset, limit);
        return comments;
    }

    @Override
    public int selectCountByEntity(int entityType, int entityId) {
        return commentMapper.selectCountByEntity(entityType,entityId);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED,propagation = Propagation.REQUIRED)
    public int insertComment(Comment comment) {

        if (comment == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }

//        添加评论
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        comment.setContent(sensitiveFilter.filter(comment.getContent()));

        int rows = commentMapper.insertComment(comment);

        if (comment.getEntityType()== ENTITY_TYPE_POST){
            int count = commentMapper.selectCountByEntity(comment.getEntityType(), comment.getEntityId());
            discussPostMapper.updateCommentCount(comment.getEntityId(),count);
        }
        return rows;
    }

    @Override
    public Comment selectCommentById(int id) {
        return commentMapper.selectCommentById(id);
    }

    @Override
    public List<Comment> selectCommentByUserId(int userId,int offset,int limit) {
        return commentMapper.selectCommentByUserId(userId,offset,limit);
    }

    @Override
    public int selectCommentCountByUserId(int userId) {
        return commentMapper.selectCommentCountById(userId);
    }

    @Override
    public List<Comment> selectCommentByEntityId(int entityId) {
        return commentMapper.selectCommentByEntityId(entityId);
    }

    @Override
    public int updateCommentStatus(int commentId, int status) {
        return commentMapper.updateCommentStatus(commentId,status);
    }

    @Override
    public int updateListCommentStatus(List<Integer> commentsId, int status) {
        return commentMapper.updateListCommentStatus(commentsId,status);
    }
}
