package com.xuecheng.api.search;

import com.xuecheng.framework.domain.course.CoursePub;
import com.xuecheng.framework.domain.course.response.TeachplanMediaPub;
import com.xuecheng.framework.domain.search.CourseSearchParam;
import com.xuecheng.framework.model.response.QueryResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import java.util.Map;

@Api(value = "课程搜索",description = "课程搜索",tags = {"课程搜索"})
public interface EsCourseControllerApi {
    //课程搜索
    @ApiOperation("课程搜索")
    QueryResponseResult<CoursePub> list(int page, int size, CourseSearchParam courseSearchParam);

    //用于视频播放界面
    @ApiOperation("根据课程ID查询课程信息")
    Map<String,CoursePub> getall(String courseId);

    @ApiOperation("根据课程计划ID查询课程媒资信息")
    TeachplanMediaPub getmedia(String id);
}
