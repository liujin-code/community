package com.liu.community.controller;

import com.liu.community.entity.User;
import com.liu.community.service.UserService;
import com.liu.community.utils.ActivateConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Map;

@Controller
public class LoginController implements ActivateConstant {

    @Autowired
    private UserService userService;

    @RequestMapping(path = "/register", method = RequestMethod.GET)
    public String getRegisterPage() {
        return "/site/register";
    }

    @RequestMapping(path = "/login", method = RequestMethod.GET)
    public String getLoginPage() {
        return "/site/login";
    }

    @RequestMapping(path = "/register",method = RequestMethod.POST)
    public String register(Model model, User user){
        Map<String, Object> map = userService.register(user);
        if (map.isEmpty()||map==null){
            model.addAttribute("msg","您的账号已经注册成功,请点击链接激活!");
            model.addAttribute("target","/index");
            return "/site/operate-result";
        }else {
            model.addAttribute("emailMsg",map.get("emailMsg"));
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            return "/site/register";
        }
    }

    // http://localhost:8080/community/activation/101/code
    @RequestMapping(path = "/activation/{id}/{code}" ,method = RequestMethod.GET)
    public String activation(Model model , @PathVariable("id") int id, @PathVariable("code") String code){
        int i = userService.activateAccount(id, code);
        if (i==ACTIVATE_SUCCESS){
            model.addAttribute("msg","您的账号已经激活成功,可以正常使用了!");
            model.addAttribute("target","/login");
            return "/site/operate-result";
        }
        else if (i==ACTIVATION_REPEAT){
            model.addAttribute("msg","您的账号已经激活过了,无需重复激活!");
            model.addAttribute("target","/index");
            return "/site/operate-result";
        }
       else if (i==ACTIVATION_FAIL){
            model.addAttribute("msg","激活失败，激活码不对!");
            model.addAttribute("target","/index");
            return "/site/operate-result";
        }
        model.addAttribute("msg","未知错误!");
        model.addAttribute("target","/index");
        return "/site/operate-result";
    }
}
