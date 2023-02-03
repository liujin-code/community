package com.liu.community.controller;

import com.liu.community.entity.DiscussPost;
import com.liu.community.entity.Page;
import com.liu.community.entity.SearchResult;
import com.liu.community.service.ElasticService;
import com.liu.community.service.LikeService;
import com.liu.community.service.UserService;
import com.liu.community.utils.CommunityConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SearchController implements CommunityConstant {
    private static final Logger logger = LoggerFactory.getLogger(SearchController.class);
    @Autowired
    private ElasticService elasticService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public String search(String keyword, Page page , Model model){
//
//        page.setLimit(3);
////      搜索帖子
//        List<DiscussPost> searchResult = elasticService.searchDiscussPost(keyword, page.getCurrent() - 1, page.getLimit());
//
//        List<Map<String,Object>> list = new ArrayList<>();
//
//        if (searchResult!=null){
//            for (DiscussPost discussPost : searchResult){
//                Map<String,Object> map = new HashMap<>();
//                // 帖子
//                map.put("post", discussPost);
//                // 作者
//                map.put("user", userService.selectUserById(discussPost.getUserId()));
//                // 点赞数量
//                map.put("likeCount", likeService.entityLikeCount(ENTITY_TYPE_POST, discussPost.getId()));
//
//                list.add(map);
//            }
//        }
//        model.addAttribute("discussPosts", list);
//        model.addAttribute("keyword", keyword);
//
//        page.setPath("/search?keyword=" + keyword);
//        page.setRows(searchResult == null ? 0 : (int) searchResult.size());
        try {
            SearchResult searchResult = elasticService.searchDiscussPost(keyword, (page.getCurrent() - 1)*10, page.getLimit());
            List<Map<String,Object>> discussPosts = new ArrayList<>();
            List<DiscussPost> list = searchResult.getList();
            if(list != null) {
                for (DiscussPost post : list) {
                    Map<String,Object> map = new HashMap<>();
                    //帖子 和 作者
                    map.put("post",post);
                    map.put("user",userService.selectUserById(post.getUserId()));
                    // 点赞数目
                    map.put("likeCount",likeService.entityLikeCount(ENTITY_TYPE_POST, post.getId()));

                    discussPosts.add(map);
                }
            }
            model.addAttribute("discussPosts",discussPosts);
            model.addAttribute("keyword",keyword);
            //分页信息
            page.setPath("/search?keyword=" + keyword);
            page.setRows(searchResult.getTotal() == 0 ? 0 : (int) searchResult.getTotal());
        } catch (IOException e) {
            logger.error("系统出错，没有数据：" + e.getMessage());
        }

        return "/site/search";
    }

}
