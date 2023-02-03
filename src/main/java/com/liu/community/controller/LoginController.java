package com.liu.community.controller;

import com.google.code.kaptcha.Producer;
import com.liu.community.config.CaptchaConfiguration;
import com.liu.community.entity.User;
import com.liu.community.service.UserService;
import com.liu.community.utils.CommunityConstant;
import com.liu.community.utils.CommunityUtil;
import com.liu.community.utils.CookieUtils;
import com.liu.community.utils.RedisKeyUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class LoginController implements CommunityConstant {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private CaptchaConfiguration captchaConfiguration;

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @RequestMapping(path = "/register", method = RequestMethod.GET)
    public String getRegisterPage() {
        return "/site/register";
    }

    @RequestMapping(path = "/login", method = RequestMethod.GET)
    public String getLoginPage() {
        return "/site/login";
    }

    @RequestMapping(value = "/forget",method = RequestMethod.GET)
    public String getForgetPage(){
        return "/site/forget";
    }

    @RequestMapping(value = "/forgetCode",method = RequestMethod.POST)
    @ResponseBody
    public String getForgetCode(String email,Model model){
        if (StringUtils.isBlank(email)){
            return CommunityUtil.getJsonString(500,"邮箱不能为空");
        }

//        已经发送过code
        String forgetKey = RedisKeyUtils.getForgetKey(email);
        String code = (String) redisTemplate.opsForValue().get(forgetKey);
        System.out.println(code);
        if (!StringUtils.isBlank(code)){
//            model.addAttribute("codeMsg","已经发送过验证码到您的邮箱，请到邮箱查看!");
            return CommunityUtil.getJsonString(500,"已经发送过验证码到您的邮箱，请到邮箱查看!");
        }
        Map<String, Object> map = userService.getForgetCode(email);
        if (!map.containsKey("code")){
            return CommunityUtil.getJsonString(500,(String)map.get("emailMsg"));
//            model.addAttribute("emailMsg",map.get("emailMsg"));
        }else{
//            model.addAttribute("codeMsg","验证码已经发送到您的邮箱!");
            return CommunityUtil.getJsonString(0,"验证码发送成功，请注意在邮箱查收!");
        }
    }

    @RequestMapping(value = "/forget",method = RequestMethod.POST)
    public String forget(String email,String code,String password,Model model){
        if (StringUtils.isBlank(email)||StringUtils.isBlank(code)||StringUtils.isBlank(password)){
            model.addAttribute("errorMsg","请输入数据");
            model.addAttribute("email",email);
            return "/site/forget";
        }
//        查找验证码
        String forgetKey = RedisKeyUtils.getForgetKey(email);
        String verifyCode = (String) redisTemplate.opsForValue().get(forgetKey);
        if (verifyCode==null){
            model.addAttribute("codeMsg","验证码已过期，请点击按钮重新发送！");
            model.addAttribute("email",email);
            return "/site/forget";
        }
        if (!verifyCode.equals(code)){
            model.addAttribute("codeMsg","验证码错误！");
            model.addAttribute("email",email);
            return "/site/forget";
        }
        User user = userService.selectUserByEmail(email);
        userService.updatePassword(user.getId(), password);
        model.addAttribute("msg","修改密码成功，请登陆！");
        model.addAttribute("target","/login");
        return "/site/operate-result";
    }
    @RequestMapping(path = "/register",method = RequestMethod.POST)
    public String register(Model model, User user){
        Map<String, Object> map = userService.register(user);
        if (map.isEmpty()||map==null){
            model.addAttribute("msg","注册成功,我们已经向您的邮箱发送了一封激活邮件,请尽快激活!");
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
    public void generateCaptcha(HttpServletResponse response/*,HttpSession session*/){
        Producer producer = captchaConfiguration.captchaProducer();

//        生成验证码
        String text = producer.createText();
//        根据验证码生成图片
        BufferedImage image = producer.createImage(text);
//        存储验证码，登陆时检验
//        session.setAttribute("captcha",text);

        // 验证码的归属
        String owner = CommunityUtil.generateUUID();
        Cookie cookie = new Cookie("captchaOwner", owner);
        cookie.setMaxAge(60);
        cookie.setPath(contextPath);
        response.addCookie(cookie);

//      存入redis中
        String captchaKey = RedisKeyUtils.getCaptchaKey(owner);
        redisTemplate.opsForValue().set(captchaKey,text,60, TimeUnit.SECONDS);

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
    @RequestMapping(path = "/login",method = RequestMethod.POST)
    public String login(String username, String password, String code, Model model,
                        boolean rememberMe/*,HttpSession session*/, HttpServletResponse response, HttpServletRequest request/*@CookieValue("captchaOwner")String captchaOwner*/)  {
//        String captcha = (String) session.getAttribute("captcha");
        String captcha = null;
        String captchaOwner = CookieUtils.getValue(request, "captchaOwner");
        if (captchaOwner==null){
            model.addAttribute("codeMsg", "验证码已过期!");
            return "/site/login";
        }
//      从redis中获取captcha
        if (!StringUtils.isBlank(captchaOwner)){
            String captchaKey = RedisKeyUtils.getCaptchaKey(captchaOwner);
            captcha = (String)redisTemplate.opsForValue().get(captchaKey);
        }

        if (StringUtils.isBlank(captcha) || StringUtils.isBlank(code) || !captcha.equalsIgnoreCase(code)) {
            model.addAttribute("codeMsg", "验证码不正确!");
            return "/site/login";
        }
        int expireSeconds = rememberMe?REMEMBER_EXPIRED_SECONDS:DEFAULT_EXPIRED_SECONDS;
        Map<String, Object> map = userService.login(username, password, (long) expireSeconds);
        if (map.containsKey("ticket")){
            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
            cookie.setPath(contextPath);
            cookie.setMaxAge(expireSeconds);
            response.addCookie(cookie);
            return "redirect:/index";
        }else{
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "/site/login";
        }
    }

    @RequestMapping(path = "/logout",method = RequestMethod.GET)
    public String logOut(@CookieValue String ticket){
        userService.logout(ticket);
        SecurityContextHolder.clearContext();
        return "redirect:/index";
    }
}
