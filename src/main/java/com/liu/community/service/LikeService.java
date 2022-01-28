package com.liu.community.service;

public interface LikeService {
    void like(int userId, int entityType, int entityId, int targetUserId);

    long entityLikeCount(int entityType, int entityId);

    int entityLikeStatus(int entityType, int entityId, int userId);

    long UserLikeCount(int userId);
}
