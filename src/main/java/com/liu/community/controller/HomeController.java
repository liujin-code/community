package com.liu.community.controller;

import com.liu.community.entity.DiscussPost;
import com.liu.community.entity.Page;
import com.liu.community.entity.User;
import com.liu.community.service.DiscussPostService;
import com.liu.community.service.LikeService;
import com.liu.community.service.UserService;
import com.liu.community.utils.CommunityConstant;
import org.aspectj.lang.annotation.RequiredTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Controller
public class HomeController implements CommunityConstant {

    @Autowired
    private UserService userService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private LikeService likeService;

    @RequestMapping(path = "/", method = RequestMethod.GET)
    public String root(){
        return "forward:/index";
    }
    @RequestMapping(path = "/index", method = RequestMethod.GET)
    public String getIndexPage(Model model, Page page, @RequestParam(name = "orderMode",defaultValue = "0") int orderMode) {
        // 方法调用钱,SpringMVC会自动实例化Model和Page,并将Page注入Model.
        // 所以,在thymeleaf中可以直接访问Page对象中的数据.
        page.setPath("/index?orderMode="+orderMode);
        page.setRows(discussPostService.selectDiscussPostRows(0));

        List<DiscussPost> list = discussPostService.selectDiscussPosts(0, page.getOffset(), page.getLimit(),orderMode);
        List<Map<String, Object>> discussPosts = new ArrayList<>();

        for (DiscussPost post : list) {
            Map<String, Object> map = new HashMap<>();
            map.put("post", post);
            User user = userService.selectUserById(post.getUserId());
            map.put("user", user);

//            评论数量
            long likeCount = likeService.entityLikeCount(ENTITY_TYPE_POST, post.getId());
            map.put("likeCount", likeCount);
            int userStatus = likeService.entityLikeStatus(ENTITY_TYPE_POST, post.getId(), user.getId());
            map.put("userStatus",userStatus);

            discussPosts.add(map);
        }

        model.addAttribute("discussPosts", discussPosts);
        model.addAttribute("orderMode",orderMode);
        return "/index";
    }

    @RequestMapping(path = "/error", method = RequestMethod.GET)
    public String getErrorPage() {
        return "/error/500";
    }

    // 拒绝访问时的提示页面
    @RequestMapping(path = "/denied", method = RequestMethod.GET)
    public String getDeniedPage() {
        return "/error/404";
    }
}
