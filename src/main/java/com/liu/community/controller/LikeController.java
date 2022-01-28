package com.liu.community.controller;

import com.liu.community.entity.Event;
import com.liu.community.entity.User;
import com.liu.community.event.EventProducer;
import com.liu.community.service.LikeService;
import com.liu.community.utils.CommunityConstant;
import com.liu.community.utils.CommunityUtil;
import com.liu.community.utils.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class LikeController implements CommunityConstant {

    @Autowired
    private LikeService likeService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private EventProducer eventProducer;

    @RequestMapping(path = "/like", method = RequestMethod.POST)
    @ResponseBody
    public String Like(int entityType, int entityId, int targetUserId,int postId) {

        User user = hostHolder.getUser();

        likeService.like(user.getId(), entityType, entityId, targetUserId);
        long likeCount = likeService.entityLikeCount(entityType, entityId);
        int likeStatus = likeService.entityLikeStatus(entityType, entityId, user.getId());

        Map<String, Object> map = new HashMap<>();

        map.put("likeCount", likeCount);
        map.put("likeStatus", likeStatus);

        // 触发点赞事件
        if (likeStatus == 1) {
            Event event = new Event()
                    .setTopic(TOPIC_LIKE)
                    .setUserId(hostHolder.getUser().getId())
                    .setEntityType(entityType)
                    .setEntityId(entityId)
                    .setEntityUserId(targetUserId)
                    .setData("postId", postId);
            eventProducer.handlerEvent(event);
        }

        return CommunityUtil.getJsonString(0, null, map);
    }
}
