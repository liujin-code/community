package com.liu.community;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class WkTest {

    @Test
    public void test(){
        String cmd = "/Volumes/software/wkhtmltox/bin/wkhtmltoimage --quality 75 https://www.baidu.com /Volumes/software/work/wk-images/3.png";
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            if(p.waitFor() == 0){
                System.out.println("ok");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
