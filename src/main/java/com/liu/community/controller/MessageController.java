package com.liu.community.controller;

import com.alibaba.fastjson.JSONObject;
import com.liu.community.entity.Message;
import com.liu.community.entity.Page;
import com.liu.community.entity.User;
import com.liu.community.service.MessageService;
import com.liu.community.service.UserService;
import com.liu.community.utils.CommunityConstant;
import com.liu.community.utils.CommunityUtil;
import com.liu.community.utils.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import java.util.*;

@Controller
public class MessageController implements CommunityConstant {

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @RequestMapping(path = "/letter/list", method = RequestMethod.GET)
    public String listLetters(Model model, Page page) {

        User user = hostHolder.getUser();
        page.setPath("/letter/list");
        page.setLimit(10);
        page.setRows(messageService.selectConversationCount(user.getId()));

        List<Map<String, Object>> lists = new ArrayList<>();

        List<Message> messages = messageService.selectConversationById(user.getId(), page.getOffset(), page.getLimit());

        if (messages != null) {
            for (Message message : messages) {
                Map<String, Object> map = new HashMap<>();
                map.put("conversation", message);
                map.put("letterCount", messageService.selectLettersCount(message.getConversationId()));
                map.put("unreadCount", messageService.selectLetterUnreadCount(user.getId(), message.getConversationId()));
                int target = user.getId() == message.getFromId() ? message.getToId() : message.getFromId();
                map.put("target", userService.selectUserById(target));
                lists.add(map);
            }
        }

        model.addAttribute("lists", lists);
        // 查询未读消息数量
        int letterUnreadCount = messageService.selectLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);
        int noticeUnreadCount = messageService.selectNoticeUnreadCount(user.getId(), null);
        model.addAttribute("noticeUnreadCount", noticeUnreadCount);
        return "/site/letter";
    }

    @RequestMapping(path = "/letter/detail/{id}", method = RequestMethod.GET)
    public String listLetterDetail(@PathVariable("id") String id, Model model, Page page) {
        page.setPath("/letter/detail/" + id);
        page.setLimit(6);
        page.setRows(messageService.selectLettersCount(id));

        List<Message> messages = messageService.selectLetters(id, page.getOffset(), page.getLimit());
        List<Map<String, Object>> lists = new ArrayList<>();
        if (messages != null) {
            for (Message message : messages) {
                Map<String, Object> map = new HashMap<>();

                map.put("message", message);
                map.put("fromUser", userService.selectUserById(message.getFromId()));

                lists.add(map);
            }

            model.addAttribute("messages", lists);
            // 私信目标
            model.addAttribute("target", getTarget(id));

            List<Integer> ids = getLetterIds(messages);

//            System.out.println(ids);
            if (ids != null && ids.size() != 0) {
                messageService.updateStatus(ids, 1);
            }
        }
        return "/site/letter-detail";
    }

    private User getTarget(String id) {
        String[] split = id.split("_");

        int id0 = Integer.parseInt(split[0]);
        int id1 = Integer.parseInt(split[1]);

        int userId = hostHolder.getUser().getId();
        if (userId == id0) {
            return userService.selectUserById(id1);
        } else {
            return userService.selectUserById(id0);
        }
    }

    private List<Integer> getLetterIds(List<Message> letterList) {
        List<Integer> ids = new ArrayList<>();

        if (letterList != null) {
            for (Message message : letterList) {
                if (hostHolder.getUser().getId() == message.getToId() && message.getStatus() == 0) {
                    ids.add(message.getId());
                }
            }
        }
        return ids;
    }

    @RequestMapping(path = "/letter/send", method = RequestMethod.POST)
    @ResponseBody
    public String sendMsg(String toName, String content) {
        User target = userService.selectUserByName(toName);
        User user = hostHolder.getUser();

        if (target == null) {
            return CommunityUtil.getJsonString(1, "您所发送消息的对象不存在，请检查发送对象用户名是否正确");
        }

        Message message = new Message();
        message.setFromId(user.getId());
        message.setToId(target.getId());
        message.setStatus(0);
        message.setContent(content);
        message.setCreateTime(new Date());

        String conversationId = user.getId() < target.getId() ? user.getId() + "_" + target.getId() : target.getId() + "_" + user.getId();
        System.out.println(conversationId);

        message.setConversationId(conversationId);
        messageService.insertMessage(message);
        return CommunityUtil.getJsonString(0);
    }

    @RequestMapping(value = "/notice/list", method = RequestMethod.GET)
    public String getNoticeList(Model model) {
        User user = hostHolder.getUser();

        //  评论通知
        Message comment = messageService.selectLastNotice(user.getId(), TOPIC_COMMENT);
        if (comment != null) {
            Map<String, Object> commentVo = new HashMap<>();
            commentVo.put("comment", comment);

            String content = HtmlUtils.htmlUnescape(comment.getContent());
            HashMap<String, Object> data = JSONObject.parseObject(content, HashMap.class);

            commentVo.put("user", userService.selectUserById((Integer) data.get("userId")));
            commentVo.put("entityType", data.get("entityType"));
            commentVo.put("entityId", data.get("entityId"));
            commentVo.put("postId", data.get("postId"));

            int count = messageService.selectNoticeCount(user.getId(), TOPIC_COMMENT);
            commentVo.put("count", count);

            int unreadCount = messageService.selectNoticeUnreadCount(user.getId(), TOPIC_COMMENT);
            commentVo.put("unreadCount", unreadCount);
            model.addAttribute("commentVo", commentVo);
        }

        //  点赞通知
        Message like = messageService.selectLastNotice(user.getId(), TOPIC_LIKE);
        if (like != null) {
            Map<String, Object> likeVo = new HashMap<>();
            likeVo.put("like", like);

            String content = HtmlUtils.htmlUnescape(like.getContent());
            HashMap<String, Object> data = JSONObject.parseObject(content, HashMap.class);

            likeVo.put("user", userService.selectUserById((Integer) data.get("userId")));
            likeVo.put("entityId", data.get("entityId"));
            likeVo.put("entityType", data.get("entityType"));
            likeVo.put("postId", data.get("postId"));

            int count = messageService.selectNoticeCount(user.getId(), TOPIC_LIKE);
            likeVo.put("count", count);

            int unreadCount = messageService.selectNoticeUnreadCount(user.getId(), TOPIC_LIKE);
            likeVo.put("unreadCount", unreadCount);
            model.addAttribute("likeVo", likeVo);
        }

//       关注通知
        Message follow = messageService.selectLastNotice(user.getId(), TOPIC_FOLLOW);
        if (follow != null) {
            Map<String, Object> followVo = new HashMap<>();
            followVo.put("follow", follow);

            String content = HtmlUtils.htmlUnescape(follow.getContent());
            HashMap<String, Object> data = JSONObject.parseObject(content, HashMap.class);

            followVo.put("user", userService.selectUserById((Integer) data.get("userId")));
            followVo.put("entityId", data.get("entityId"));
            followVo.put("entityType", data.get("entityType"));

            int count = messageService.selectNoticeCount(user.getId(), TOPIC_FOLLOW);
            followVo.put("count", count);

            int unreadCount = messageService.selectNoticeUnreadCount(user.getId(), TOPIC_FOLLOW);
            followVo.put("unreadCount", unreadCount);
            model.addAttribute("followVo", followVo);
        }

        int letterUnreadCount = messageService.selectLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount",letterUnreadCount);
        int noticeUnreadCount = messageService.selectNoticeUnreadCount(user.getId(), null);
        model.addAttribute("noticeUnreadCount",noticeUnreadCount);

        return "/site/notice";
    }

    @RequestMapping(value = "/notice/detail/{topic}",method = RequestMethod.GET)
    public String getNoticeDetail(@PathVariable("topic") String topic,Page page,Model model){
        int noticeCount = messageService.selectNoticeCount(hostHolder.getUser().getId(), topic);
        page.setLimit(10);
        page.setRows(noticeCount);
        page.setPath("/notice/detail/"+topic);

        List<Message> messages = messageService.selectNoticeDetail(hostHolder.getUser().getId(), topic, page.getOffset(), page.getLimit());
        List<HashMap<String,Object>> messagesVo = new ArrayList<>();
        if (messages!=null){

            for (Message message : messages){
                HashMap<String,Object> map = new HashMap<>();
                map.put("message",message);
                map.put("fromUser",userService.selectUserById(message.getFromId()));

                String content = HtmlUtils.htmlUnescape(message.getContent());
                HashMap<String,Object> data = JSONObject.parseObject(content, HashMap.class);
                map.put("user", userService.selectUserById((Integer) data.get("userId")));
                map.put("entityId",data.get("entityId"));
                map.put("entityType",data.get("entityType"));
                map.put("postId",data.get("postId"));

                messagesVo.add(map);
            }

        }

        model.addAttribute("notice",messagesVo);

        List<Integer> ids = getLetterIds(messages);
        if (ids!=null&&ids.size()!=0){
            messageService.updateStatus(ids,1);
        }
        return "/site/notice-detail";
    }
}
