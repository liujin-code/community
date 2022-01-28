package com.liu.community.controller;

import com.liu.community.dao.CommentMapper;
import com.liu.community.entity.Comment;
import com.liu.community.entity.User;
import com.liu.community.service.CommentService;
import com.liu.community.utils.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@Controller
@RequestMapping("/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;
    @Autowired
    private HostHolder hostHolder;

    @RequestMapping(path = "/add/{id}",method = RequestMethod.POST)
    public String add(@PathVariable("id") int id, Comment comment, Model model){
        User user = hostHolder.getUser();
        comment.setUserId(user.getId());
        comment.setCreateTime(new Date());
        comment.setStatus(0);
        commentService.insertComment(comment);
        return "redirect:/discuss/detail/"+id;
    }
}
