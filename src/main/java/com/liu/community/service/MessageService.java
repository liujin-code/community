package com.liu.community.service;

import com.liu.community.entity.Message;

import java.util.List;

public interface MessageService {
    List<Message> selectConversationById(int userId, int offset, int limit);

    int selectConversationCount(int userId);

    List<Message> selectLetters(String conversationId,int offset,int limit);

    int selectLettersCount(String conversationId);

    // 查询未读私信的数量
    int selectLetterUnreadCount(int userId, String conversationId);

    // 新增消息
    int insertMessage(Message message);

    // 修改消息的状态
    int updateStatus(List<Integer> ids, int status);
}
