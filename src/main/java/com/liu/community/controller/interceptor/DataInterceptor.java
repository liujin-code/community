package com.liu.community.controller.interceptor;

import com.liu.community.entity.User;
import com.liu.community.service.DataService;
import com.liu.community.utils.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class DataInterceptor implements HandlerInterceptor {

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private DataService dataService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        统计UV
        String ip = request.getRemoteHost();
        dataService.recordUV(ip);

//        统计DUV
        User user = hostHolder.getUser();
        if (user!=null){
            dataService.recordDAU(user.getId());
        }
        return true;
    }
}
