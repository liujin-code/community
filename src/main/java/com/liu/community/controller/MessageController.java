package com.liu.community.controller;

import com.liu.community.entity.Message;
import com.liu.community.entity.Page;
import com.liu.community.entity.User;
import com.liu.community.service.MessageService;
import com.liu.community.service.UserService;
import com.liu.community.utils.CommunityUtil;
import com.liu.community.utils.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller
@RequestMapping("/letter")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @RequestMapping(path = "/list", method = RequestMethod.GET)
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

        return "/site/letter";
    }

    @RequestMapping(path = "/detail/{id}", method = RequestMethod.GET)
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

    @RequestMapping(path = "/send", method = RequestMethod.POST)
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
}
