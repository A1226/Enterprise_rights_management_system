package com.xuecheng.govern.gateway.service;

import com.xuecheng.framework.utils.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

@Service
public class AuthService {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    //从头取出jwt令牌
    public String getJwtFromHeader(HttpServletRequest request){
        //获取头信息
        String authorization = request.getHeader("Authorization");
        if (StringUtils.isEmpty(authorization)){
            return null;//拒绝访问
        }
        //如果不是以Bearer 开头的
        if (!authorization.startsWith("Bearer ")){
            return null;//拒绝访问
        }
        //从第7位截取
        String jwt = authorization.substring(7);
        return jwt;
    }
    //从cookie取出token
    public String getTokenFromCookie(HttpServletRequest request){
        Map<String, String> map = CookieUtil.readCookie(request, "uid");
        String uid = map.get("uid");
        if (StringUtils.isEmpty(uid)){
            return null;
        }
        return uid;
    }
    //从Redis查询令牌有效期
    public long getExpire(String token){
        String key = "user_token:"+token;
        //有效期getExpire 获取秒TimeUnit.SECONDS
        Long expire = stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
        return expire;
    }

}
