package com.liu.community.controller;

import com.liu.community.entity.Comment;
import com.liu.community.entity.DiscussPost;
import com.liu.community.entity.Event;
import com.liu.community.entity.User;
import com.liu.community.event.EventProducer;
import com.liu.community.service.CommentService;
import com.liu.community.service.DiscussPostService;
import com.liu.community.utils.CommunityConstant;
import com.liu.community.utils.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@Controller
@RequestMapping("/comment")
public class CommentController implements CommunityConstant {

    @Autowired
    private CommentService commentService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private DiscussPostService discussPostService;


    @RequestMapping(path = "/add/{id}",method = RequestMethod.POST)
    public String add(@PathVariable("id") int id, Comment comment){
        User user = hostHolder.getUser();
        comment.setUserId(user.getId());
        comment.setCreateTime(new Date());
        comment.setStatus(0);
        commentService.insertComment(comment);

//      触发评论事件
        Event event = new Event();
        event.setTopic(TOPIC_COMMENT)
                .setUserId(hostHolder.getUser().getId())
                .setEntityId(comment.getEntityId())
                .setEntityType(comment.getEntityType())
                .setData("postId", id);

        if (event.getEntityType()==ENTITY_TYPE_POST){
            DiscussPost discussPost = discussPostService.selectDiscussPostById(id);
            event.setEntityUserId(discussPost.getUserId());
        }else if (event.getEntityType()==ENTITY_TYPE_COMMENT){
            Comment comment1 = commentService.selectCommentById(comment.getEntityId());
            event.setEntityUserId(comment1.getUserId());
        }
        eventProducer.handlerEvent(event);

        if (comment.getEntityType() == ENTITY_TYPE_POST) {
            // 触发发帖事件
            event = new Event()
                    .setTopic(TOPIC_PUBLISH)
                    .setUserId(comment.getUserId())
                    .setEntityType(ENTITY_TYPE_POST)
                    .setEntityId(id);
            eventProducer.handlerEvent(event);
        }

        return "redirect:/discuss/detail/"+id;
    }
}
