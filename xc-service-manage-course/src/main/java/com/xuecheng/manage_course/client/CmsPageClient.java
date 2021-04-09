package com.xuecheng.manage_course.client;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(value = "XC-SERVICE-MANAGE-CMS") //指定远程调用的服务名
public interface CmsPageClient {

    //根据ID查询课程页面 信息
    @GetMapping("/cms/page/get/{id}") //用@GetMapping标识远程调用的http类型
    CmsPage findCmsPageById(@PathVariable("id") String id);

    //添加页面，用于远程预览接口
    @PostMapping("/cms/page/save")
    CmsPageResult saveCmaPage(@RequestBody CmsPage cmsPage);

    //一键发布
    @PostMapping("/cms/page/postPageQuick")
    CmsPostPageResult postPageQuick(@RequestBody CmsPage cmsPage);


}
