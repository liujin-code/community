package com.liu.community.service.Impl;

import com.liu.community.dao.UserMapper;
import com.liu.community.entity.User;
import com.liu.community.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;
    @Override
    public User selectUserById(int userId) {
        return userMapper.selectById(userId);
    }
}
