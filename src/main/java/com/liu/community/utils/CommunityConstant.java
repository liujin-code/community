package com.liu.community.utils;

public interface CommunityConstant {
    //    激活成功
    int ACTIVATE_SUCCESS = 0;
    //    重复激活
    int ACTIVATION_REPEAT = 1;
    //    激活成功
    int ACTIVATION_FAIL = 2;
    /**
     * 默认状态的登录凭证的超时时间
     * 12h
     */
    int DEFAULT_EXPIRED_SECONDS = 12 * 60 * 60;

    /**
     * 记住状态的登录凭证超时时间
     * 100day
     */
    int REMEMBER_EXPIRED_SECONDS = 100 * 24 * 60 * 60;
    /**
     * 实体类型: 帖子评论
     */
    int ENTITY_TYPE_POST = 1;

    /**
     * 实体类型: 评论的评论
     */
    int ENTITY_TYPE_COMMENT = 2;
}
