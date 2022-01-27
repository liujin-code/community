package com.liu.community.controller.interceptor;

import com.liu.community.annotation.LoginRequired;
import com.liu.community.entity.User;
import com.liu.community.utils.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class LoginRequiredInterceptor implements HandlerInterceptor {

    @Autowired
    private HostHolder  hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod){
            User user = hostHolder.getUser();
            HandlerMethod method = (HandlerMethod) handler;
            LoginRequired methodAnnotation = method.getMethodAnnotation(LoginRequired.class);
            if (methodAnnotation!=null&&user==null){
                System.out.println(request.getContextPath());
                response.sendRedirect(request.getContextPath()+"/login");
                return false;
            }
        }

        return true;
    }

}
