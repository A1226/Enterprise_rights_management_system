package com.xuecheng.auth;

import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@RunWith(SpringRunner.class)
public class redisTest {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    public void testRedis(){
        //定义key
        String key = "user_token:028d2e2e-5c81-4495-801c-df101223d235";
        //定义value
        Map<String,String> map = new HashMap<>();
        map.put("refresh_token","eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJjb21wYW55SWQiOm51bGwsInVzZXJwaWMiOm51bGwsInVzZXJfbmFtZSI6Iml0Y2FzdCIsInNjb3BlIjpbImFwcCJdLCJhdGkiOiIwMjhkMmUyZS01YzgxLTQ0OTUtODAxYy1kZjEwMTIyM2QyMzUiLCJuYW1lIjpudWxsLCJ1dHlwZSI6bnVsbCwiaWQiOm51bGwsImV4cCI6MTYxNjg4OTU3MywianRpIjoiNzM1MzE2OTUtZTM5Ny00ZTAzLThlNDctMjE5ODRmN2U4YzA1IiwiY2xpZW50X2lkIjoiWGNXZWJBcHAifQ.l85V49haMLOEbtbopEwtHOhRlOevLcJ1rPAx0rlCa4l8STmcFBQoYFjJvyE8FWEL3rKPFCPcUi9DKd3jkv8wntNjXkPW27vTF-VOqrge84TazRa8gLRD-ub47Afg_w7HD7Lf7Qxs0mEXZYnF379YeFiyghpnrFRbdKK-lN7-l7JT_B052b7spmJPEBB0zI9WbLOx6Si5A0exVTkaA48tbeW47wVUNsssmFsvlj71MyLolaEFDVIgALz3hITRoalC09fzeaKzvjk_MVDITR_O4hYD1jmSlpqPFuGk72ytL1oqh3DgmOqoT-Sx8rKzCNFzw1Xy-UW2SO5Alty-nQYhoA");
        //转换为json
        String jsonString = JSON.toJSONString(map);
        //存储数据
        stringRedisTemplate.boundValueOps(key).set(jsonString,60, TimeUnit.SECONDS);
        //读取过期时间，已过期返回-2
        Long expire = stringRedisTemplate.getExpire(key);
        System.out.println(expire);
        //根据key获取value
        String value = stringRedisTemplate.opsForValue().get(key);
        System.out.println(value);

    }
}
