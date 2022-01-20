package com.liu.community.service;

import com.liu.community.entity.User;

import java.util.Map;

public interface UserService {
    public User selectUserById(int userId);

    public Map<String, Object> register(User user);

    public int activateAccount(int userId,String code);
}
