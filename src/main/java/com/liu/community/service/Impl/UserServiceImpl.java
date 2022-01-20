package com.liu.community.service.Impl;

import com.liu.community.dao.UserMapper;
import com.liu.community.entity.User;
import com.liu.community.service.UserService;
import com.liu.community.utils.ActivateConstant;
import com.liu.community.utils.EmailUtils;
import com.liu.community.utils.RegisterUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class UserServiceImpl implements UserService, ActivateConstant {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private EmailUtils emailUtils;

    @Autowired
    private RegisterUtil registerUtil;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String serverContext;

    @Override
    public User selectUserById(int userId) {
        return userMapper.selectById(userId);
    }

    @Override
    public Map<String, Object> register(User user) {
        Map<String, Object> map = new HashMap<>();
        String username = user.getUsername();
        String password = user.getPassword();
        String email = user.getEmail();

        // 空值处理
        if (user == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }
        if (StringUtils.isBlank(username)) {
            map.put("usernameMsg", "输入的用户名不能为空");
            return map;
        }
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "输入的密码不能为空");
            return map;
        }
        if (StringUtils.isBlank(email)) {
            map.put("emailMsg", "输入的邮箱不能为空");
            return map;
        }

//        判断输入参数是否重复
        User u = userMapper.selectByName(username);
        if (u != null) {
            map.put("usernameMsg", "输入的用户名已存在");
            return map;
        }
        u = userMapper.selectByEmail(email);
        if (u != null) {
            map.put("emailMsg", "输入的邮箱已存在");
            return map;
        }

//        注册操作
        user.setSalt(registerUtil.generateUUID().substring(0, 6));
        user.setPassword(registerUtil.generateMD5Key(password + user.getSalt()));
        user.setStatus(0);
        user.setType(0);
        user.setCreateTime(new Date());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setActivationCode(registerUtil.generateUUID().replace("-", ""));
        userMapper.insertUser(user);

//        激活操作
        Context context = new Context();
        context.setVariable("email", email);
        // http://localhost:8080/community/activation/101/code
        String url = domain + serverContext + "/activation/" + user.getId() + "/" + user.getActivationCode();
        System.out.println(url);
        context.setVariable("url", url);
        String process = templateEngine.process("/mail/activation", context);
        emailUtils.sendMail(user.getEmail(), "激活账号", process);
        return map;
    }

    @Override
    public int activateAccount(int userId, String code) {
        User user = selectUserById(userId);
        if (user.getStatus() == 1) {
            return ACTIVATION_REPEAT;
        } else if (code.equals(user.getActivationCode())) {
            userMapper.updateStatus(userId, 1);
            return ACTIVATE_SUCCESS;
        } else {
            return ACTIVATION_FAIL;
        }
    }
}
