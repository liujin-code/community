package com.liu.community.service.Impl;

import com.liu.community.service.LikeService;
import com.liu.community.utils.RedisKeyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

@Service
public class LikeServiceImpl implements LikeService {

    @Autowired
    private RedisTemplate redisTemplate;

    //    点赞
    public void like(int userId, int entityType, int entityId, int targetUserId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String entityLikeKey = RedisKeyUtils.getEntityLike(entityType, entityId);
                String userLikeKey = RedisKeyUtils.getUserLike(targetUserId);

                boolean isMember = operations.opsForSet().isMember(entityLikeKey, userId);

                operations.multi();
                if (isMember) {
                    redisTemplate.opsForSet().remove(entityLikeKey, userId);
                    redisTemplate.opsForValue().decrement(userLikeKey);

                } else {
                    redisTemplate.opsForSet().add(entityLikeKey, userId);
                    redisTemplate.opsForValue().increment(userLikeKey);
                }
                return operations.exec();
            }
        });
    }

    //    查询实体点赞数量
    public long entityLikeCount(int entityType, int entityId) {
        String entityLike = RedisKeyUtils.getEntityLike(entityType, entityId);
//        System.out.println("entityLike=" +entityLike);
        Long size = redisTemplate.opsForSet().size(entityLike);
        return size;
    }

    //    查询某人对实体的点赞状态
    public int entityLikeStatus(int entityType, int entityId, int userId) {
        String entityLike = RedisKeyUtils.getEntityLike(entityType, entityId);
        Boolean member = redisTemplate.opsForSet().isMember(entityLike, userId);
//        System.out.println("entityLike=" +entityLike+"userId"+userId+" meber:"+member);

        return member ? 1 : 0;
    }

    public long UserLikeCount(int userId) {
        String userLike = RedisKeyUtils.getUserLike(userId);
        Integer count = (Integer) redisTemplate.opsForValue().get(userLike);
        return count == null ? 0 : count;
    }
}
