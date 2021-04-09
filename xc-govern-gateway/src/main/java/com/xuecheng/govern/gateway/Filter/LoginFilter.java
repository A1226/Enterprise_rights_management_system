package com.xuecheng.govern.gateway.Filter;

import com.alibaba.fastjson.JSON;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.govern.gateway.service.AuthService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Component
public class LoginFilter extends ZuulFilter {

    @Autowired
    AuthService authService;

    /**
     filterType：返回字符串代表过滤器的类型，如下 pre：请求在被路由之前执行 routing：在路由请求时调用
     post：在routing和errror过滤器之后调用 error：处理请求时发生错误调用
     * @return
     */
    @Override
    public String filterType() {
        return "pre";
    }

    /**
     * filterOrder：此方法返回整型数值，通过此数值来定义过滤器的执行顺序，数字越小优先级越高
     * @return
     */
    @Override
    public int filterOrder() {
        return 0;
    }

    /**
     * shouldFilter：返回一个Boolean值，判断该过滤器是否需要执行。
        返回true表示要执行此过虑器，否则不执行
     * @return
     */
    @Override
    public boolean shouldFilter() {
        return true;
    }

    /**
     * 过滤器的业务逻辑
     * 过虑所有请求，判断头部信息是否有Authorization，如果没有则拒绝访问，否则转发到微服务。
     * @return
     * @throws ZuulException
     */
    @Override
    public Object run() throws ZuulException {
        RequestContext requestContext = RequestContext.getCurrentContext();
        HttpServletRequest request = requestContext.getRequest();
        //判断响应头
        String jwtFromHeader = authService.getJwtFromHeader(request);
        if (StringUtils.isEmpty(jwtFromHeader)){
            this.access_denied(requestContext);
            return null;
        }
        //判断cookie
        String tokenFromCookie = authService.getTokenFromCookie(request);
        if (StringUtils.isEmpty(tokenFromCookie)){
            this.access_denied(requestContext);
            return null;
        }
        //Redis判断jwt有效期
        long expire = authService.getExpire(tokenFromCookie);
        if (expire < 0){
            this.access_denied(requestContext);
            return null;
        }

        return null;
    }

    private void access_denied(RequestContext requestContext){
        requestContext.setSendZuulResponse(false);// 拒绝访问
        // 设置响应状态码
        requestContext.setResponseStatusCode(200);
        //响应消息体
        ResponseResult responseResult = new ResponseResult(CommonCode.UNAUTHENTICATED);
        String jsonString = JSON.toJSONString(responseResult);
        requestContext.getResponse().setContentType("application/json;charset=UTF-8");
        requestContext.setResponseBody(jsonString);

    }
}
