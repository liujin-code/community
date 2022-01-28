package com.liu.community.controller;

import com.liu.community.entity.*;
import com.liu.community.event.EventProducer;
import com.liu.community.service.CommentService;
import com.liu.community.service.DiscussPostService;
import com.liu.community.service.LikeService;
import com.liu.community.service.UserService;
import com.liu.community.utils.CommunityConstant;
import com.liu.community.utils.CommunityUtil;
import com.liu.community.utils.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private EventProducer eventProducer;

    @RequestMapping(path = "/add", method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title, String content) {
        User user = hostHolder.getUser();
        if (user == null) {
            return CommunityUtil.getJsonString(403, "你还没有登录哦!");
        }

        DiscussPost discussPost = new DiscussPost();
        discussPost.setTitle(title);
        discussPost.setContent(content);
        discussPost.setUserId(user.getId());
        discussPost.setCreateTime(new Date());
        discussPost.setStatus(0);
        discussPost.setType(0);

        discussPostService.insertDiscussPost(discussPost);

//        触发评论事件
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(discussPost.getId())
                .setUserId(hostHolder.getUser().getId());

        eventProducer.handlerEvent(event);
        return CommunityUtil.getJsonString(0, "发布成功!");
    }

    @RequestMapping(path = "/detail/{id}", method = RequestMethod.GET)
    public String detail(Model model, @PathVariable("id") int id, Page page) {
//        获取帖子信息
        DiscussPost discussPost = discussPostService.selectDiscussPostById(id);

        model.addAttribute("post", discussPost);

        User user = userService.selectUserById(discussPost.getUserId());

        model.addAttribute("user", user);

        // 点赞数量
        long likeCount = likeService.entityLikeCount(ENTITY_TYPE_POST, id);
        model.addAttribute("likeCount", likeCount);
        // 点赞状态
        int likeStatus = hostHolder.getUser() == null ? 0 :
                likeService.entityLikeStatus( ENTITY_TYPE_POST,id, hostHolder.getUser().getId());
        model.addAttribute("likeStatus", likeStatus);
//        获取帖子评论
        //评论总数
        int commentCount = discussPost.getCommentCount();
        page.setPath("/discuss/detail/" + id);
        page.setRows(commentCount);
        page.setLimit(5);
        //所有评论
        List<Comment> comments = commentService.selectCommentByEntity(ENTITY_TYPE_POST, id, page.getOffset(), page.getLimit());
        // 评论列表,帖子评论放一个map中，评论的评论放一个list中
        List<Map<String, Object>> commentList = new ArrayList<>();
        if (comments != null) {
            for (Comment comment : comments) {
                //帖子评论
                Map<String, Object> postMap = new HashMap<>();
//                存放评论
                postMap.put("comment", comment);
//                存放用户
                postMap.put("user", userService.selectUserById(comment.getUserId()));
//                评论数量
                likeCount = likeService.entityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
                postMap.put("likeCount", likeCount);
//                评论状态
                likeStatus = hostHolder.getUser() == null ? 0 :
                        likeService.entityLikeStatus(ENTITY_TYPE_COMMENT, comment.getId(), hostHolder.getUser().getId());
                postMap.put("userStatus", likeStatus);
//                System.out.println("count="+likeCount);
//                System.out.println("status="+likeStatus);
//                回复的评论
                List<Comment> replyComments = commentService.selectCommentByEntity(ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);
                // 回复评论列表
                List<Map<String, Object>> replyList = new ArrayList<>();
                if (replyComments != null) {
                    for (Comment replyComment : replyComments) {
                        //评论的评论
                        Map<String, Object> replyMap = new HashMap<>();

                        replyMap.put("comment", replyComment);
                        replyMap.put("user", userService.selectUserById(replyComment.getUserId()));
                        User target = replyComment.getTargetId() == 0 ? null : userService.selectUserById(replyComment.getTargetId());
                        replyMap.put("target", target);
                        int reReplyCount = commentService.selectCountByEntity(ENTITY_TYPE_COMMENT, replyComment.getId());
                        postMap.put("reReplyCount", reReplyCount);
                        //评论数量
                        long Count = likeService.entityLikeCount(ENTITY_TYPE_COMMENT, replyComment.getId());
//                        System.out.println("count="+Count);
                        replyMap.put("likeCount", Count);
                        //评论状态
                        int status = hostHolder.getUser() == null ? 0 :
                                likeService.entityLikeStatus(ENTITY_TYPE_COMMENT, replyComment.getId(),  hostHolder.getUser().getId());
//                        System.out.println("status="+status);
                        replyMap.put("userStatus", status);

                        replyList.add(replyMap);
                    }
                }
                postMap.put("replys", replyList);

                // 回复数量
                int replyCount = commentService.selectCountByEntity(ENTITY_TYPE_COMMENT, comment.getId());
                postMap.put("replyCount", replyCount);
                commentList.add(postMap);
            }

        }
        model.addAttribute("comments", commentList);
        return "/site/discuss-detail";
    }
}
