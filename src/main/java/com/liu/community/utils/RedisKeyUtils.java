package com.liu.community.utils;

public class RedisKeyUtils {

    private final static String SPLIT = ":";
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    private static final String PREFIX_USER_LIKE = "like:user";
    private static final String PREFIX_FOLLOWEE = "followee";
    private static final String PREFIX_FOLLOWER = "follower";
    private static final String PREFIX_CAPTCHA = "captcha";
    private static final String PREFIX_TICKET = "ticket";
    private static final String PREFIX_USER = "user";
    private static final String PREFIX_UV = "uv";
    private static final String PREFIX_DAU = "dau";
    private static final String PREFIX_POST = "post";
    private static final String PREFIX_FORGET = "forget";

    //    实体的赞
    public static String getEntityLike(int entityType, int entityId) {
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }

    //    用户的赞
    public static String getUserLike(int userId){
        return PREFIX_USER_LIKE+SPLIT+userId;
    }

    // 某个用户关注的实体
    // followee:userId:entityType -> zset(entityId,now)
    public static String getFolloweeKey(int userId, int entityType){
        return PREFIX_FOLLOWEE+SPLIT+userId+SPLIT+entityType;
    }

    // 某个实体拥有的粉丝
    // follower:entityType:entityId -> zset(userId,now)
    public static String getFollowerKey(int entityType, int entityId){
        return PREFIX_FOLLOWER+SPLIT+entityType+SPLIT+entityId;
    }

//    获取验证码的key
    public static String getCaptchaKey(String owner){
        return PREFIX_CAPTCHA+SPLIT+owner;
    }
//    获取登陆凭证的key
    public static String getTicketKey(String ticket){
        return PREFIX_TICKET+SPLIT+ticket;
    }
//    获取用户的key
    public static String getUserKey(int userId){
        return PREFIX_USER+SPLIT+userId;
    }
//    获取每日访问用户的key
    public static String getUvKye(String date){
        return PREFIX_UV+SPLIT+date;
    }
    public static String getUvKey(String start,String end){
        return PREFIX_UV+SPLIT+start+SPLIT+end;
    }
//    获取日活key
    public static String getDauKey(String date){
        return PREFIX_DAU+SPLIT+date;
    }
    public static String getDauKey(String start,String end){
        return PREFIX_DAU+SPLIT+start+SPLIT+end;
    }
//    帖子分数
    public static String getPostScoreKey(){
        return PREFIX_POST+SPLIT+"score";
    }
//    找回密码
    public static String getForgetKey(String email){
        return PREFIX_FORGET+SPLIT+email;
    }
}
