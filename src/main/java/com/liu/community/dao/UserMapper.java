package com.liu.community.dao;

import com.liu.community.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    User selectById(int id);

    User selectByName(String username);

    User selectByEmail(String email);

    int insertUser(User user);

    int updateStatus(int id, int status);

    int updateHeadUrl(int id, String headerUrl);

    int updatePassword(int id, String password);

    int updateHeader(int userId, String headerUrl);
}
