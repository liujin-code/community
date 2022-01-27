package com.liu.community.dao;

import com.liu.community.entity.LoginTicket;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LoginTicketMapper {

    LoginTicket selectByTicket(String ticket);

    int insertLoginTicket(LoginTicket loginTicket);

    int updateLoginTicket(String ticket,int status);

    LoginTicket selectByUserId(int userId);
}
