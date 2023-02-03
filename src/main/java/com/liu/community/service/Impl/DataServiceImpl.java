package com.liu.community.service.Impl;

import com.liu.community.service.DataService;
import com.liu.community.utils.RedisKeyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class DataServiceImpl implements DataService {

    @Autowired
    private RedisTemplate redisTemplate;

    private SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");

//    记录访问的用户
    @Override
    public void recordUV(String ip) {
        String uvKye = RedisKeyUtils.getUvKye(df.format(new Date()));
        redisTemplate.opsForHyperLogLog().add(uvKye,ip);
    }

    @Override
    public long calculateUV(Date start, Date end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }
//        统计人数
        List<String> keyList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        while(!calendar.getTime().after(end)){
            String key = RedisKeyUtils.getUvKye(df.format(calendar.getTime()));
            keyList.add(key);
            calendar.add(Calendar.DATE,1);
        }
        String uvKey = RedisKeyUtils.getUvKey(df.format(start), df.format(end));
        redisTemplate.opsForHyperLogLog().union(uvKey,keyList.toArray());
        return redisTemplate.opsForHyperLogLog().size(uvKey);
    }

    @Override
    public void recordDAU(int userId) {
        String dauKey = RedisKeyUtils.getDauKey(df.format(new Date()));
        redisTemplate.opsForValue().setBit(dauKey,userId,true);
    }

    @Override
    public long calculateDAU(Date start, Date end) {
        if (start==null||end==null){
            throw new IllegalArgumentException("参数不能为空!");
        }
        Calendar calendar = Calendar.getInstance();
        List<byte[]> list = new ArrayList<>();
        calendar.setTime(start);
        while (!calendar.getTime().after(end)){
            String dauKey = RedisKeyUtils.getDauKey(df.format(calendar.getTime()));
            list.add(dauKey.getBytes());
            calendar.add(calendar.DATE,1);
        }

        return (long)redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                String dauKey = RedisKeyUtils.getDauKey(df.format(start), df.format(end));
                connection.bitOp(RedisStringCommands.BitOperation.OR,
                        dauKey.getBytes(), list.toArray(new byte[0][0]));
                return connection.bitCount(dauKey.getBytes());
            }
        });

    }
}
