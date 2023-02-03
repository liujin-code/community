package com.liu.community.controller;

import com.liu.community.entity.Event;
import com.liu.community.entity.User;
import com.liu.community.event.EventProducer;
import com.liu.community.service.UserService;
import com.liu.community.utils.CommunityConstant;
import com.liu.community.utils.CommunityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ManageController implements CommunityConstant {

    @Autowired
    private UserService userService;


    @RequestMapping(value = "/managed",method = RequestMethod.POST)
    @ResponseBody
    public String setManage(int userId){
        User user = userService.selectUserById(userId);
        System.out.println(userId+"userType"+user.getType());
        if (user.getType()==1){
            userService.updateUserType(userId,0);
            return CommunityUtil.getJsonString(0,"已取消管理员权限");
        }else{
            userService.updateUserType(userId,1);
            return CommunityUtil.getJsonString(0,"已设置为管理员");
        }
    }
}
