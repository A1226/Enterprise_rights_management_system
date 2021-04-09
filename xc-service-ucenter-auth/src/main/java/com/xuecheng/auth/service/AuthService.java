package com.xuecheng.auth.service;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.client.XcServiceList;
import com.xuecheng.framework.domain.ucenter.ext.AuthToken;


import com.xuecheng.framework.domain.ucenter.ext.UserTokenStore;
import com.xuecheng.framework.domain.ucenter.response.AuthCode;
import com.xuecheng.framework.exception.ExceptionCast;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.stereotype.Service;

import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class AuthService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private LoadBalancerClient loadBalancerClient;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Value("${auth.tokenValiditySeconds}")
    private int tokenValiditySeconds;//Redis过期时间
    /**
     * 用户申请令牌
     * @param username
     * @param password
     * @param clientId
     * @param clientSecret
     * @return
     */
    public AuthToken login(String username, String password, String clientId, String clientSecret) {

        //向spring security 申请认证
        AuthToken authToken = this.applyToken(username, password, clientId, clientSecret);
        if (authToken == null){
            ExceptionCast.cast(AuthCode.AUTH_LOGIN_APPLYTOKEN_FAIL);
        }
        //向Redis存储令牌信息
        //用户身份令牌
        String access_token = authToken.getAccess_token();
        //内容
        String content = JSON.toJSONString(authToken);
        boolean saveToken = this.saveToken(access_token, content, tokenValiditySeconds);
        if (!saveToken){
            ExceptionCast.cast(AuthCode.AUTH_LOGIN_TOKEN_SAVEFAIL);
        }

        return authToken;
    }


    /**
     *  //向Redis存储令牌信息
     * @param access_token 用户身份令牌
     * @param content 内容就是AuthToken对象的内容
     * @param ttl 过期时间
     * @return
     */
    private boolean saveToken(String access_token,String content,long ttl){
        String key = "user_token:" + access_token;
        stringRedisTemplate.boundValueOps(key).set(content,ttl, TimeUnit.SECONDS);
        //检查成功或者失败
        Long expire = stringRedisTemplate.getExpire(key,TimeUnit.SECONDS);
        return expire>0;//失败会返回小于0的数
    }

    //删除Redis信息
    public boolean delToken(String access_token){
        String key = "user_token:" + access_token;
        stringRedisTemplate.delete(key);
        //查询
        stringRedisTemplate.getExpire(key);
        return true;
    }

    //向Redis查询令牌
    public AuthToken getUserToken(String token){
        String userToken  = "user_token:" + token;
        //获取令牌
        String userTokenString = stringRedisTemplate.opsForValue().get(userToken);
        try {
            AuthToken authToken = JSON.parseObject(userTokenString, AuthToken.class);
            return authToken;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //申请令牌
    private AuthToken applyToken(String username,String password,String clientId,String
            clientSecret){
        //从eureka中获取认证服务地址
        ServiceInstance serviceInstance = loadBalancerClient.choose(XcServiceList.XC_SERVICE_UCENTER_AUTH);
        //这个地址就是http://ip:port
        URI uri = serviceInstance.getUri();
        //令牌申请的地址
        String authUrl = uri + "/auth/oauth/token";

        //定义Header
        LinkedMultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        String httpBasic = getHttpBasic(clientId, clientSecret);
        header.add("Authorization",httpBasic);

        //定义body
        LinkedMultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type","password");
        body.add("username",username);
        body.add("password",password);

        HttpEntity<MultiValueMap<String,String>> httpEntity = new HttpEntity<MultiValueMap<String, String>>(body, header);

        restTemplate.setErrorHandler(new DefaultResponseErrorHandler(){
            @Override
            public void handleError(URI url, HttpMethod method, ClientHttpResponse response) throws IOException {
                if (response.getRawStatusCode()!=400 && response.getRawStatusCode()!=401){
                    super.handleError(response);
                }
            }
        });

        ResponseEntity<Map> exchange = restTemplate.exchange(authUrl, HttpMethod.POST, httpEntity, Map.class);
        //申请令牌的信息
        Map bodyMap = exchange.getBody();
        if (bodyMap == null || bodyMap.get("access_token") == null
                || bodyMap.get("refresh_token") == null
                || bodyMap.get("jti") == null){
            if (bodyMap != null && bodyMap.get("error_description")!=null){
                String error_description = (String) bodyMap.get("error_description");
                if (error_description.indexOf("UserDetailsService returned null")>=0){
                    ExceptionCast.cast(AuthCode.AUTH_ACCOUNT_NOTEXISTS);
                }
                if (error_description.indexOf("坏的凭证")>=0){
                    ExceptionCast.cast(AuthCode.AUTH_CREDENTIAL_ERROR);
                }
            }
            return  null;
        }


        AuthToken authToken = new AuthToken();
        authToken.setAccess_token((String) bodyMap.get("jti"));//用户身份令牌
        authToken.setRefresh_token((String) bodyMap.get("refresh_token"));//刷新令牌
        authToken.setJwt_token((String) bodyMap.get("access_token"));//jwt令牌

        return authToken;

    }

    private String getHttpBasic(String clientId,String clientSecret){
        String string = clientId + ":" + clientSecret;
        //将字符串进行base64编码
        byte[] encode = Base64Utils.encode(string.getBytes());
        return "Basic " + new String(encode);
    }
}
