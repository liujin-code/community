package com.liu.community.controller.interceptor;

import com.liu.community.entity.LoginTicket;
import com.liu.community.entity.User;
import com.liu.community.service.MessageService;
import com.liu.community.service.UserService;
import com.liu.community.utils.CookieUtils;
import com.liu.community.utils.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
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
//        从cookie中查询凭证
        String ticket = CookieUtils.getValue(request, "ticket");

        if (ticket!=null){
//            查询凭证
            LoginTicket loginTicket = userService.selectLoginTicketByTicket(ticket);
//            凭证是否有效
            if (loginTicket!=null&&loginTicket.getStatus()==1&&loginTicket.getExpired().after(new Date())){
//                根据凭证查询用户
                User user = userService.selectUserById(loginTicket.getUserId());
//                在本次请求中持有用户
                hostHolder.setUser(user);
                // 构建用户认证的结果,并存入SecurityContext,以便于Security进行授权.
                Authentication authentication = new UsernamePasswordAuthenticationToken(
//                        principal: 主要信息; credentials: 证书; authorities: 权限;
                        user, user.getPassword(), userService.getAuthorities(user.getId())
                );
                SecurityContextHolder.setContext(new SecurityContextImpl(authentication));
            }
        }
        return true;
    }

//调用controller后，渲染thymeleaf模版之前，根据是否有user向前端模版传user值的参数
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();
        if (user!=null&&modelAndView!=null){
            modelAndView.addObject("loginUser",user);
        }
    }

//渲染完thymeleaf模版之后
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        hostHolder.remove();
//        SecurityContextHolder.clearContext();
    }
}
