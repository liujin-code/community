package com.liu.community.utils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.util.UUID;

@Component
public class RegisterUtil {

    public static String generateUUID() {
        return UUID.randomUUID().toString();
    }

    public static String generateMD5Key(String key) {
        if (StringUtils.isBlank(key)){
            return null;
        }
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }
}
