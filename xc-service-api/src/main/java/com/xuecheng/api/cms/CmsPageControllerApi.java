package com.xuecheng.api.cms;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

/**
 * Api工程的接口将作为各微服务远程调用使用
 *
 * 页面查询接口
 */
@Api(value="cms页面管理接口",description = "cms页面管理接口，提供页面的增、删、改、查")
public interface CmsPageControllerApi {
    //页面查询
    @ApiOperation("分页查询页面列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name="page",value = "页码",required=true,paramType="path",dataType="int"),
            @ApiImplicitParam(name="size",value = "每页记录数",required=true,paramType="path",dataType="int")
    })
    QueryResponseResult findList(int page,int size, QueryPageRequest queryPageRequest);

    //新增页面
    @ApiOperation("新增页面")
    CmsPageResult add(CmsPage cmsPage);

    //根据ID查询页面信息
    @ApiOperation("根据ID查询页面信息")
    CmsPage findById(String pageId);

    //根据ID查询页面信息并修改信息
    @ApiOperation("根据ID查询页面信息并修改信息")
    CmsPageResult edit(String id,CmsPage cmsPage);

    //根据ID删除页面
    @ApiOperation("根据ID删除页面")
    ResponseResult delete(String pageId);

    //页面发布
    @ApiOperation("页面发布")
    ResponseResult post(String pageId);

    //保存页面
    @ApiOperation("保存页面")
    CmsPageResult save(CmsPage cmsPage);

    @ApiOperation("一键发布页面")
    CmsPostPageResult postPageQuick(CmsPage cmsPage);

}
