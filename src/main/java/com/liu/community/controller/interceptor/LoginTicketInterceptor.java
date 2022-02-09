package com.liu.community.controller.interceptor;

import com.liu.community.entity.LoginTicket;
import com.liu.community.entity.User;
import com.liu.community.service.MessageService;
import com.liu.community.service.UserService;
import com.liu.community.utils.CookieUtils;
import com.liu.community.utils.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Component
public class LoginTicketInterceptor implements HandlerInterceptor {
    @Autowired
    private UserService userService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private HostHolder hostHolder;

//    调用controller前，查询是否有用户登陆令牌并根据令牌是否生效添加user
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String ticket = CookieUtils.getValue(request, "ticket");
        if (ticket!=null){
            LoginTicket loginTicket = userService.selectLoginTicketByTicket(ticket);
            if (loginTicket!=null&&loginTicket.getStatus()==1&&loginTicket.getExpired().after(new Date())){
                User user = userService.selectUserById(loginTicket.getUserId());
                hostHolder.setUser(user);
            }
        }
        return true;
    }

//调用controller后，渲染thymeleaf模版之前，根据是否有user向前端模版传user值的参数
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();
        if (user!=null&&modelAndView!=null){
            int unreadCount = messageService.selectLetterUnreadCount(user.getId(), null);
            modelAndView.addObject("loginUser",user);
            modelAndView.addObject("unreadLetter",unreadCount);
        }
    }

//渲染完thymeleaf模版之后
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        hostHolder.remove();
    }
}
