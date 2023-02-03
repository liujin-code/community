package com.liu.community.service;

import com.liu.community.entity.LoginTicket;
import com.liu.community.entity.User;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Map;

public interface UserService {
    public User selectUserById(int userId);

    public User selectUserByEmail(String email);

    public Map<String, Object> register(User user);

    public int activateAccount(int userId, String code);

    public Map<String, Object> login(String username, String password, Long expiredSeconds);

    public void logout(String ticket);

    public LoginTicket selectLoginTicketByTicket(String ticket);

    public int updateHeader(int userId, String headerUrl);

    public int updatePassword(int userId, String password);

    User selectUserByName(String toName);

    public Collection<? extends GrantedAuthority> getAuthorities(int userId);

    public Map<String,Object> getForgetCode(String email);

    public int updateUserType(int userId , int type);
}
