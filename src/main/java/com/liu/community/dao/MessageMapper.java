package com.liu.community.dao;

import com.liu.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MessageMapper {

    List<Message> selectConversationById(int userId, int offset, int limit);

    int selectConversationCount(int userId);

    List<Message> selectLetters(String conversationId, int offset, int limit);

    int selectLettersCount(String conversationId);

    // 查询未读私信的数量
    int selectLetterUnreadCount(int userId, String conversationId);

    // 新增消息
    int insertMessage(Message message);

    // 修改消息的状态
    int updateStatus(List<Integer> ids, int status);

    // 未读通知数量
    int selectNoticeUnreadCount(int userId, String topic);

    //  通知数量
    int selectNoticeCount(int userId, String topic);

    //  最后一条通知
    Message selectLastNotice(int userId, String topic);

    //  查询某一主题的详细通知
    List<Message> selectNoticeDetail(int userId, String topic, int offset, int limit);
}
