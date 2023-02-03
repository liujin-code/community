package com.liu.community.controller;

import com.liu.community.entity.Event;
import com.liu.community.event.EventProducer;
import com.liu.community.utils.CommunityConstant;
import com.liu.community.utils.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

@Controller
public class WkController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(WkController.class);

    @Autowired
    private EventProducer eventProducer;

    @Value("${community.path.domain}")
    private String domain;

    @Value(("${server.servlet.context-path}"))
    private String contextPath;

    @Value("${wk.images.storage}")
    private String wkImageStorage;

    @Value("${qiniu.bucket.share.url}")
    private String shareBucketUrl;

    @RequestMapping(value = "/share",method = RequestMethod.GET)
    @ResponseBody
    public String share(String htmlUrl){

        String fileName = CommunityUtil.generateUUID();

//        异步处理图片分享
        Event event = new Event().setTopic(TOPIC_SHARE)
                .setData("htmlUrl",htmlUrl)
                .setData("fileName",fileName)
                .setData("suffix",".png");
        eventProducer.handlerEvent(event);

        // 返回访问路径
        Map<String, Object> map = new HashMap<>();
//        map.put("shareUrl", domain + contextPath + "/share/image/" + fileName);
        map.put("shareUrl", shareBucketUrl + "/"  + fileName);
        return CommunityUtil.getJsonString(0,null,map);
    }

//    废弃
    @RequestMapping(value = "/share/image/{fileName}",method = RequestMethod.GET)
    public void getImage(@PathVariable("fileName") String fileName, HttpServletResponse response){

        File file = new File(wkImageStorage + "/" + fileName + ".png");

        response.setContentType("image/png");

        try(FileInputStream inputStream = new FileInputStream(file)) {

            ServletOutputStream outputStream = response.getOutputStream();
            byte[] buffer = new byte[1024];
            int b = 0;
            while((b=inputStream.read(buffer))!=-0){
                outputStream.write(buffer,0,b);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            logger.error("获取长图失败: " + e.getMessage());
        }
    }
}
