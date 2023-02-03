package com.liu.community.controller.interceptor;

import com.liu.community.service.MessageService;
import com.liu.community.utils.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class MessageInterceptor implements HandlerInterceptor {

    @Autowired
    private MessageService messageService;

    @Autowired
    private HostHolder hostHolder;

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        if (hostHolder.getUser()!=null&&modelAndView!=null){
            int letterUnreadCount = messageService.selectLetterUnreadCount(hostHolder.getUser().getId(), null);
            int noticeUnreadCount = messageService.selectNoticeUnreadCount(hostHolder.getUser().getId(), null);
            modelAndView.addObject("allUnreadCount", letterUnreadCount + noticeUnreadCount);
        }
    }
}
