package com.liu.community.event;

import com.alibaba.fastjson.JSONObject;
import com.liu.community.entity.DiscussPost;
import com.liu.community.entity.Event;
import com.liu.community.entity.Message;
import com.liu.community.service.DiscussPostService;
import com.liu.community.service.ElasticService;
import com.liu.community.service.MessageService;
import com.liu.community.utils.CommunityConstant;
import com.liu.community.utils.CommunityUtil;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;

@Component
public class EventConsumer implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    @Autowired
    private MessageService messageService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private ElasticService elasticService;

    @Value("${wk.image.command}")
    private String wkImageCommand;

    @Value("${wk.images.storage}")
    private String wkImageStorage;

    @Value("${qiniu.key.access}")
    private String accessKey;

    @Value("${qiniu.key.secret}")
    private String secretKey;

    @Value("${qiniu.bucket.share.name}")
    private String shareBucketName;

    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;

    @KafkaListener(topics = {TOPIC_COMMENT,TOPIC_FOLLOW,TOPIC_LIKE})
    public void handlerMessage(ConsumerRecord record){
        if (record==null||record.value()==null){
            logger.error("消息的内容为空!");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(), Event.class);

        if (event == null) {
            logger.error("消息格式错误!");
            return;
        }

        Message message = new Message();
        message.setFromId(SYSTEM_USER_ID);
        message.setToId(event.getEntityUserId());
        message.setConversationId(event.getTopic());
        message.setCreateTime(new Date());

        Map<String,Object> content = new HashMap<>();
        content.put("entityType",event.getEntityType());
        content.put("entityId",event.getEntityId());
        content.put("userId",event.getUserId());

        if (event.getData()!=null){
            for (Map.Entry<String,Object> entry : event.getData().entrySet()){
                content.put(entry.getKey(),entry.getValue());
            }
        }

        message.setContent(JSONObject.toJSONString(content));
        messageService.insertMessage(message);
    }

//    发帖消费者
    @KafkaListener(topics = TOPIC_PUBLISH)
    public void handlePublishMessage(ConsumerRecord record){
        if (record==null||record.value()==null){
            logger.error("消息的内容为空!");
            return;
        }
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);

        if (event == null) {
            logger.error("消息格式错误!");
            return;
        }

        DiscussPost discussPost = discussPostService.selectDiscussPostById(event.getEntityId());
        elasticService.save(discussPost);
    }

//    删帖消费者
    @KafkaListener(topics = TOPIC_DELETE)
    public void handlerDeleteComment(ConsumerRecord record){
        if (record==null||record.value()==null){
            logger.error("消费的内容为空！");
            return;
        }
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event==null){
            logger.error("消息格式错误");
            return;
        }
        elasticService.delete(event.getEntityId());
    }
//   分享消费者
    @KafkaListener(topics = TOPIC_SHARE)
    public void handlerImageShare(ConsumerRecord record){
        if (record==null||record.value()==null){
            logger.error("消费的内容为空！");
            return;
        }
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);

        String htmlUrl = (String) event.getData().get("htmlUrl");
        String fileName = (String) event.getData().get("fileName");
        String suffix = (String) event.getData().get("suffix");

        String cmd = wkImageCommand + " --quality 75 "
                + htmlUrl + " " + wkImageStorage + "/" + fileName + suffix;
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            if(p.waitFor() == 0){
                logger.info("生成长图成功: " + cmd);
            }
        } catch (Exception e) {
            logger.error("生成长图失败: " + e.getMessage());
        }

        // 启用定时器,监视该图片,一旦生成了,则上传至七牛云.
        uploadTask task = new uploadTask(fileName, suffix);
        Future future = taskScheduler.scheduleAtFixedRate(task, 500);
        task.setFuture(future);
    }

    class uploadTask implements Runnable{
        // 文件名称
        private String fileName;
        // 文件后缀
        private String suffix;
        // 启动任务的返回值
        private Future future;
        // 开始时间
        private long startTime;
        // 上传次数
        private int uploadTimes;

        public uploadTask(String fileName, String suffix) {
            this.fileName = fileName;
            this.suffix = suffix;
            startTime = System.currentTimeMillis();
        }

        public void setFuture(Future future) {
            this.future = future;
        }
        @Override
        public void run() {
            if (System.currentTimeMillis()-startTime > 30000){
                logger.error("执行时间过长,终止任务:" + fileName);
//                停止计时任务
                future.cancel(true);
                return;
            }
            if (uploadTimes>3){
                logger.error("提交次数过多，停止上传",fileName);
                future.cancel(true);
                return;
            }

            String filePath = wkImageStorage + "/" + fileName  + suffix;
            File file = new File(filePath);
            if (file.exists()){
                logger.info(String.format("开始第%d次上传[%s].", ++uploadTimes, fileName));
                //构造一个带指定 Region 对象的配置类,指定上传机房
                Configuration cfg = new Configuration(Region.region2());
                UploadManager uploadManager = new UploadManager(cfg);

                // 设置响应信息
                StringMap policy = new StringMap();
                policy.put("returnBody", CommunityUtil.getJsonString(0));

//                凭证
                Auth auth = Auth.create(accessKey,secretKey);
                String uploadToken = auth.uploadToken(shareBucketName, fileName, 3600, policy);

                try {
                    Response response = uploadManager.put(filePath, fileName, uploadToken);
                    // 处理响应结果
                    JSONObject json = JSONObject.parseObject(response.bodyString());
                    if (json == null || json.get("code") == null || !json.get("code").toString().equals("0")) {
                        logger.info(String.format("第%d次上传失败[%s].", uploadTimes, fileName));
                    } else {
                        logger.info(String.format("第%d次上传成功[%s].", uploadTimes, fileName));
                        future.cancel(true);
                    }
                } catch (QiniuException e) {
                    logger.info(String.format("第%d次上传失败[%s].", uploadTimes, fileName));
                }
            }else {
                logger.info("等待图片生成[" + fileName + "].");
            }
        }
    }

}
