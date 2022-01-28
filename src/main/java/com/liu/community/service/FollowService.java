package com.liu.community.service;

import java.util.List;
import java.util.Map;

public interface FollowService {

    public void follow(int userId, int entityType, int entityId);

    public void unfollow(int userId, int entityType, int entityId);

    public long getFolloweeCount(int userId,int entityType);

    public long getFollowerCount(int entityType,int entityId);

    public boolean hasFollowed(int userId,int entityType,int entityId);

    public List<Map<String, Object>> findFollowees(int userId, int offset, int limit);

    public List<Map<String, Object>> findFollowers(int userId, int offset, int limit);
}
