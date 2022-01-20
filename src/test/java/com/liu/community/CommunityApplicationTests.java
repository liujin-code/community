package com.liu.community;

import com.liu.community.config.CaptchaConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CommunityApplicationTests {
	@Autowired
	private CaptchaConfiguration captchaConfiguration;
	@Test
	void contextLoads() {
		captchaConfiguration.captchaProducer();
	}

}
