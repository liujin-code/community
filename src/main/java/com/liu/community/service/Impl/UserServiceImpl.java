package com.liu.community.service.Impl;

import com.liu.community.dao.LoginTicketMapper;
import com.liu.community.dao.UserMapper;
import com.liu.community.entity.LoginTicket;
import com.liu.community.entity.User;
import com.liu.community.service.UserService;
import com.liu.community.utils.CommunityConstant;
import com.liu.community.utils.EmailUtils;
import com.liu.community.utils.CommunityUtil;
import com.liu.community.utils.RedisKeyUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService, CommunityConstant {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private EmailUtils emailUtils;

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String serverContext;

    @Override
    public User selectUserById(int userId) {
//        userMapper.selectById(userId)
        User user = getCatch(userId);
        if (user==null){
            user = initCatch(userId);
        }
        return user;
    }

    @Override
    public User selectUserByEmail(String email) {
        return userMapper.selectByEmail(email);
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
        user.setSalt(CommunityUtil.generateUUID().substring(0, 6));
        user.setPassword(CommunityUtil.generateMD5Key(password + user.getSalt()));
        user.setStatus(0);
        user.setType(0);
        user.setCreateTime(new Date());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setActivationCode(CommunityUtil.generateUUID().replace("-", ""));
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
            clearCache(userId);
            return ACTIVATE_SUCCESS;
        } else {
            return ACTIVATION_FAIL;
        }
    }

    @Override
    public Map<String, Object> login(String username, String password, Long expiredSeconds) {

        Map<String, Object> map = new HashMap<>();
        // 空值处理

        if (StringUtils.isBlank(username)) {
            map.put("usernameMsg", "输入的用户名不能为空");
            return map;
        }
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "输入的密码不能为空");
            return map;
        }

        User u = userMapper.selectByName(username);
        if (u == null) {
            map.put("usernameMsg", "账号或密码错误");
            return map;
        }
        if (u.getStatus() == 0) {
            Context context = new Context();
            context.setVariable("email", u.getEmail());
            // http://localhost:8080/community/activation/101/code
            String url = domain + serverContext + "/activation/" + u.getId() + "/" + u.getActivationCode();
            System.out.println(url);
            context.setVariable("url", url);
            String process = templateEngine.process("/mail/activation", context);
            emailUtils.sendMail(u.getEmail(), "激活账号", process);
            map.put("usernameMsg", "账号未激活,已重新发送激活邮件到您的邮箱，请注意查收并激活");
            return map;
        }
        String s = CommunityUtil.generateMD5Key(password + u.getSalt());
        if (!s.equals(u.getPassword())) {
            map.put("passwordMsg", "密码错误");
            return map;
        }

//        添加登陆凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(u.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(1);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));

//        将ticket存入到redis中
        String ticketKey = RedisKeyUtils.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(ticketKey, loginTicket);
//        loginTicketMapper.insertLoginTicket(loginTicket);

        map.put("ticket", loginTicket.getTicket());
        return map;
    }

    public void logout(String ticket) {
        String ticketKey = RedisKeyUtils.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
        loginTicket.setStatus(0);
        redisTemplate.opsForValue().set(ticketKey, loginTicket);
//        loginTicketMapper.updateLoginTicket(ticket, 0);
    }

    @Override
    public LoginTicket selectLoginTicketByTicket(String ticket) {
        String ticketKey = RedisKeyUtils.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
        return loginTicket;
    }

    @Override
    public int updateHeader(int userId, String headerUrl) {
        //        return userMapper.updateHeader(userId, headerUrl);
        int rows = userMapper.updateHeader(userId, headerUrl);
        clearCache(userId);
        return rows;
    }

    @Override
    public int updatePassword(int userId, String password) {
        User user = userMapper.selectById(userId);
        String salt = user.getSalt();
        password = CommunityUtil.generateMD5Key(password+salt);
        int rows = userMapper.updatePassword(userId, password);
        clearCache(userId);
        return rows;
    }

    @Override
    public User selectUserByName(String toName) {
        return userMapper.selectByName(toName);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities(int userId) {
        User user = userMapper.selectById(userId);
        List<GrantedAuthority> list = new ArrayList<>();
        list.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                switch (user.getType()){
                    case 1:
                        return AUTHORITY_ADMIN;
                    case 2:
                        return AUTHORITY_MODERATOR;
                    default:
                        return AUTHORITY_USER;
                }
            }
        });
        return list;
    }

    @Override
    public Map<String,Object> getForgetCode(String email) {
        Map<String,Object> map = new HashMap<>();
        if (StringUtils.isBlank(email)){
           map.put("emailMsg","邮件不能为空！");
           return map;
        }
//        System.out.println(email);
        User user = userMapper.selectByEmail(email);
        if (user==null){
            map.put("emailMsg","该邮箱未注册过用户");
            return map;
        }
        String code = CommunityUtil.generateUUID().substring(0, 6);
        String forgetKey = RedisKeyUtils.getForgetKey(email);
        redisTemplate.opsForValue().set(forgetKey,code,70*5,TimeUnit.SECONDS);

        Context context = new Context();
        context.setVariable("username",user.getUsername());
        context.setVariable("code",code);
        String process = templateEngine.process("/mail/forget", context);
        emailUtils.sendMail(email,"找回密码邮件",process);
        map.put("code",code);
        return map;
    }

    @Override
    public int updateUserType(int userId, int type) {
        userMapper.updateUserType(userId,type);
        clearCache(userId);
        return userMapper.updateUserType(userId,type);
    }

    //    从缓存中获取user用户
    private User getCatch(int userId) {
        String userKey = RedisKeyUtils.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(userKey);
    }

    // 2.取不到时初始化缓存数据
    private User initCatch(int userId) {
        User user = userMapper.selectById(userId);
        String redisKey = RedisKeyUtils.getUserKey(userId);
        redisTemplate.opsForValue().set(redisKey, user, 3600, TimeUnit.SECONDS);
        return user;
    }

    // 3.数据变更时清除缓存数据
    private void clearCache(int userId) {
        String redisKey = RedisKeyUtils.getUserKey(userId);
        redisTemplate.delete(redisKey);
    }

}
