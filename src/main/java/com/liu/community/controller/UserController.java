package com.liu.community.controller;

import com.liu.community.annotation.LoginRequired;
import com.liu.community.entity.User;
import com.liu.community.service.UserService;
import com.liu.community.utils.CommunityUtil;
import com.liu.community.utils.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;

@Controller
@RequestMapping("/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${community.path.upload}")
    private String upload;

    @Autowired
    private CommunityUtil communityUtil;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @RequestMapping(path = "/setting",method = RequestMethod.GET)
    @LoginRequired
    public String getSettingPage(){
        return "/site/setting";
    }

    @RequestMapping(path = "/upload",method = RequestMethod.POST)
    @LoginRequired
    public String uploadHeader(MultipartFile fileImage , Model model){
        if (fileImage==null){
            model.addAttribute("error","您还没有选择图片!");
            return "/site/setting";
        }
        String originalFilename = fileImage.getOriginalFilename();
        String substring = originalFilename.substring(originalFilename.lastIndexOf("."));
        if (StringUtils.isBlank(substring)) {
            model.addAttribute("error", "文件的格式不正确!");
            return "/site/setting";
        }
        String fileName = communityUtil.generateUUID().replace("-","")+substring;
//        存储图片地址
        String uploadPath = upload+"/"+fileName;

        try {
            fileImage.transferTo(new File(uploadPath));
        } catch (IOException e) {
            e.printStackTrace();
        }

        User user = hostHolder.getUser();
        int userId = user.getId();
        String filePath = domain+contextPath+"/user/header/"+fileName;
        userService.updateHeader(userId,filePath);
        return "redirect:/index";
    }

    @RequestMapping(path = "/header/{fileName}",method = RequestMethod.GET)
    public void getFile(@PathVariable("fileName") String fileName, HttpServletResponse response){
//        服务器存放文件路径
        String uploadPath = upload+"/"+fileName;
        // 文件后缀
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        // 响应图片
        response.setContentType("image/" + suffix);

        try (            FileInputStream inputStream = new FileInputStream(uploadPath);
                         OutputStream outputStream = response.getOutputStream();
        ){
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, b);
            }
        } catch (IOException e) {
            logger.error("读取头像失败: " + e.getMessage());
        }
    }
}
