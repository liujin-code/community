package com.liu.community.controller;

import com.google.code.kaptcha.Producer;
import com.liu.community.config.CaptchaConfiguration;
import com.liu.community.entity.User;
import com.liu.community.service.UserService;
import com.liu.community.utils.ActivateConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;

@Controller
public class LoginController implements ActivateConstant {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private CaptchaConfiguration captchaConfiguration;

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

    @RequestMapping(path = "/captcha",method = RequestMethod.GET)
    public void generateCaptcha(HttpServletResponse response,HttpSession session){
        Producer producer = captchaConfiguration.captchaProducer();

//        生成验证码
        String text = producer.createText();
//        根据验证码生成图片
        BufferedImage image = producer.createImage(text);
//        存储验证码，登陆时检验
        session.setAttribute("captcha",text);
//        生成的是png格式
        response.setContentType("image/png");
        try {
            ServletOutputStream outputStream = response.getOutputStream();
//            传输图片
            ImageIO.write(image,"png",outputStream);
        } catch (IOException e) {
            logger.error("响应验证码失败:" + e.getMessage());
        }


    }
}
