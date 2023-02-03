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

    /**
     * 实体类型: 用户
     */
    int ENTITY_TYPE_USER = 3;

    /**
     * 主题: 评论
     */
    String TOPIC_COMMENT = "comment";

    /**
     * 主题: 点赞
     */
    String TOPIC_LIKE = "like";

    /**
     * 主题: 关注
     */
    String TOPIC_FOLLOW = "follow";

    /**
     * 主题: 发帖
     */
    String TOPIC_PUBLISH = "publish";

    /**
     * 主题: 删帖
     */
    String TOPIC_DELETE = "delete";

    /**
     * 主题: 分享
     */
    String TOPIC_SHARE = "share";

    /**
     * 主题: 分享
     */
    String TOPIC_MANAGE = "manage";

    /**
     * 系统用户ID
     */
    int SYSTEM_USER_ID = 1;

    /**
     * 权限: 普通用户
     */
    String AUTHORITY_USER = "user";

    /**
     * 权限: 管理员
     */
    String AUTHORITY_ADMIN = "admin";

    /**
     * 权限: 版主
     */
    String AUTHORITY_MODERATOR = "moderator";
}
