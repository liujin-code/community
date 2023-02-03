package com.liu.community.controller;

import com.liu.community.annotation.LoginRequired;
import com.liu.community.entity.Comment;
import com.liu.community.entity.DiscussPost;
import com.liu.community.entity.Page;
import com.liu.community.entity.User;
import com.liu.community.service.*;
import com.liu.community.utils.CommunityConstant;
import com.liu.community.utils.CommunityUtil;
import com.liu.community.utils.HostHolder;
import com.liu.community.utils.RedisKeyUtils;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${community.path.upload}")
    private String upload;

    @Value("${qiniu.key.access}")
    private String accessKey;

    @Value("${qiniu.key.secret}")
    private String secretKey;

    @Value("${qiniu.bucket.header.name}")
    private String headerBucketName;

    @Value("${quniu.bucket.header.url}")
    private String headerBucketUrl;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    @Autowired
    private DiscussPostService discusspostService;

    @Autowired
    private CommentService commentService;

    @RequestMapping(path = "/setting", method = RequestMethod.GET)
//    @LoginRequired
    public String getSettingPage(Model model) {
        String fileName = CommunityUtil.generateUUID();

        Auth auth = Auth.create(accessKey, secretKey);
        StringMap policy = new StringMap();
        policy.put("returnBody", CommunityUtil.getJsonString(0));

        String uploadToken = auth.uploadToken(headerBucketName, fileName, 3600, policy);

        model.addAttribute("uploadToken", uploadToken);
        model.addAttribute("fileName", fileName);
        return "/site/setting";
    }

    @RequestMapping("/header/url")
    @ResponseBody
    public String updateHeaderUrl(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            return CommunityUtil.getJsonString(1, "文件名不能为空!");
        }

        String url = headerBucketUrl + "/" + fileName;
        userService.updateHeader(hostHolder.getUser().getId(), url);

        return CommunityUtil.getJsonString(0);
    }

    @RequestMapping(value = "/updatePassword", method = RequestMethod.POST)
    public String updatePassword(@CookieValue String ticket, String password, String newPassword, Model model) {
        if (StringUtils.isBlank(password)) {
            throw new RuntimeException("密码不能为空");
        }
        if (StringUtils.isBlank(newPassword)) {
            throw new RuntimeException("新密码不能为空");
        }
        String oldPassword = CommunityUtil.generateMD5Key(password + hostHolder.getUser().getSalt());
        if (!oldPassword.equals(hostHolder.getUser().getPassword())) {
            model.addAttribute("passwordMsg", "旧密码错误");
            return "/site/setting";
        } else {
            userService.updatePassword(hostHolder.getUser().getId(), newPassword);
            userService.logout(ticket);
            SecurityContextHolder.clearContext();
            model.addAttribute("msg", "修改密码成功，请重新登陆");
            model.addAttribute("target", "/login");
            return "/site/operate-result";
        }
    }

    //  废弃
    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    @LoginRequired
    public String uploadHeader(MultipartFile fileImage, Model model) {
        if (fileImage == null) {
            model.addAttribute("error", "您还没有选择图片!");
            return "forward:/setting";
        }
        String originalFilename = fileImage.getOriginalFilename();
        String substring = originalFilename.substring(originalFilename.lastIndexOf("."));
        if (StringUtils.isBlank(substring)) {
            model.addAttribute("error", "文件的格式不正确!");
            return "forward:/setting";
        }
        String fileName = CommunityUtil.generateUUID().replace("-", "") + substring;
//        存储图片地址
        String uploadPath = upload + "/" + fileName;

        try {
            fileImage.transferTo(new File(uploadPath));
        } catch (IOException e) {
            e.printStackTrace();
        }

        User user = hostHolder.getUser();
        int userId = user.getId();
        String filePath = domain + contextPath + "/user/header/" + fileName;
        userService.updateHeader(userId, filePath);
        return "redirect:/index";
    }

    //  废弃
    @RequestMapping(path = "/header/{fileName}", method = RequestMethod.GET)
    public void getFile(@PathVariable("fileName") String fileName, HttpServletResponse response) {
//        服务器存放文件路径
        String uploadPath = upload + "/" + fileName;
        // 文件后缀
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        // 响应图片
        response.setContentType("image/" + suffix);

        try (FileInputStream inputStream = new FileInputStream(uploadPath);
             OutputStream outputStream = response.getOutputStream();
        ) {
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, b);
            }
        } catch (IOException e) {
            logger.error("读取头像失败: " + e.getMessage());
        }
    }

    @RequestMapping(path = "/profile/{userId}", method = RequestMethod.GET)
    public String profile(Model model, @PathVariable("userId") int userId) {
//        用户
        User user = userService.selectUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在！");
        }
        model.addAttribute("user", user);
//        关注的人的数量
        long followeeCount = followService.getFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);

//        粉丝数量
        long followerCount = followService.getFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", followerCount);

        boolean hasFollowed = false;
        if (hostHolder.getUser() != null) {
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
        }
//        System.out.println("hasFollowed"+ hasFollowed);
        model.addAttribute("hasFollowed", hasFollowed);
//        点赞数量
        long likeCount = likeService.UserLikeCount(userId);
        model.addAttribute("likeCount", likeCount);
        return "/site/profile";
    }

    @RequestMapping(value = "/myPosts/{userId}", method = RequestMethod.GET)
    public String getMyPost(@PathVariable("userId") int userId, Model model, Page page) {
//        用户
        User user = userService.selectUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在！");
        }
        //        用户发布的文章数
        int count = discusspostService.selectDiscussPostRows(userId);

        page.setPath("/user/myPosts/" + userId);
        page.setRows(count);
        page.setLimit(10);
//        用户发布的文章
        List<DiscussPost> discussPosts = discusspostService.selectDiscussPosts(userId, page.getOffset(), page.getLimit(), 0);

        List<Map<String, Object>> list = new ArrayList<>();
        if (discussPosts != null && discussPosts.size() != 0) {
            for (DiscussPost discussPost : discussPosts) {
                Map<String, Object> map = new HashMap<>();
                map.put("discussPost", discussPost);
//                文章点赞数量
                long likeCount = likeService.entityLikeCount(ENTITY_TYPE_POST, discussPost.getId());
                map.put("likeCount", likeCount);
//                截取文章前200字做展示
                String content = null;
                if (discussPost.getContent().length() < 200) {
                    content = discussPost.getContent();
                } else {
                    content = discussPost.getContent().substring(0, 200) + "....";
                }
                map.put("content", content);
                list.add(map);
            }
        }
        model.addAttribute("user", user);
        model.addAttribute("count", count);
        model.addAttribute("discussPosts", list);
        return "/site/my-post";
    }

    @RequestMapping(value = "/myReply/{userId}", method = RequestMethod.GET)
    public String getMyReply(@PathVariable("userId") int userId, Model model, Page page) {
        User user = userService.selectUserById(userId);
        User user1 = hostHolder.getUser();
        if (user == null) {
            throw new RuntimeException("该用户不存在！");
        }
        if (user.getId() != user1.getId() && (user1.getType() != 1 || user1.getType() != 2)) {
            throw new RuntimeException("用户权限不足！");
        }
//        用户评论总数
        int count = commentService.selectCommentCountByUserId(userId);
//        System.out.println(count);
//        初始化page
        page.setPath("/user/myReply/" + userId);
        page.setRows(count);
        page.setLimit(10);


        List<Comment> comments = commentService.selectCommentByUserId(userId,page.getOffset(),page.getLimit());
//        存放帖子列表
        List<Map<String, Object>> list = new ArrayList<>();
        if (comments != null && comments.size() != 0) {
            for (Comment comment : comments) {
                Map<String, Object> map = new HashMap<>();
//                用户的评论
                map.put("comment", comment);
//                用户评论的帖子
                DiscussPost discussPost = null;
//                如果是评论的评论，则先找到被评论的评论，再查询该评论所对映的文章
                if (comment.getEntityType()==ENTITY_TYPE_COMMENT){
//                    if (comment.getTargetId() != 0) {
//                        comment.getTargetId();
                        discussPost = discusspostService.selectDiscussPostById(commentService.selectCommentById(comment.getEntityId()).getEntityId());
//                    } else {
//                        discussPost = discusspostService.selectDiscussPostByIdIgnoreStatus(comment.getEntityId());
//                    }
                }else{
                    discussPost = discusspostService.selectDiscussPostByIdIgnoreStatus(comment.getEntityId());
                }
                System.out.println(discussPost);
                map.put("discussPost", discussPost);

                list.add(map);
            }
            model.addAttribute("count", count);
            model.addAttribute("comments", list);
            model.addAttribute("user", user);
        }
        return "/site/my-reply";
    }
}
