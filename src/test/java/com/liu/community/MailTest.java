package com.liu.community;

import com.liu.community.utils.EmailUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MailTest {
    @Autowired
    private EmailUtils emailUtils;

    @Autowired
    private TemplateEngine templateEngine;

    @Test
    public void sendText(){
        emailUtils.sendMail("1301605326@qq.com","这是一封来自邮箱自动发送的邮件","今天学习了javamailsender的用法，看看能否成功发送信息");
    }
    @Test
    public void testHtmlMail() {
        Context context = new Context();
        context.setVariable("username", "sunday");

        String content = templateEngine.process("/mail/demo", context);
        System.out.println(content);

        emailUtils.sendMail("1301605326@qq.com", "HTML", content);
    }

}
