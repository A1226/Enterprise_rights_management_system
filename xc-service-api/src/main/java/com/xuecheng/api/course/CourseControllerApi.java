package com.xuecheng.api.course;


import com.xuecheng.framework.domain.course.*;
import com.xuecheng.framework.domain.course.ext.CourseView;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.domain.course.response.AddCourseResult;
import com.xuecheng.framework.domain.course.response.CoursePublishResult;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;


@Api(value="课程管理接口",description = "课程管理接口，提供课程的增、删、改、查")
public interface CourseControllerApi {

    @ApiOperation("课程计划查询")
    TeachplanNode findTeachplanList(String courseId);

    @ApiOperation("添加课程计划")
    ResponseResult addTeachplan(Teachplan teachplan);

    @ApiOperation("查询我的课程列表")
    QueryResponseResult findCourseList(
            int page,
            int size,
            CourseListRequest courseListRequest
    );

    @ApiOperation("添加课程基本信息")
    AddCourseResult addCourseBase(CourseBase courseBase);

    @ApiOperation("课程基本信息查询")
    CourseBase getCourseBaseById(String courseId);

    @ApiOperation("课程基本信息更新")
    ResponseResult UpdateCourseBase(String courseId, CourseBase courseBase);

    @ApiOperation("课程营销查询")
    CourseMarket getCourseMarketById(String courseId);

    @ApiOperation("课程营销更新")
    ResponseResult UpdateCourseMarket(String courseId,CourseMarket courseMarket);

    @ApiOperation("添加课程图片")
    ResponseResult addCoursePic(String courseId,String pic);

    @ApiOperation("课程图片查询")
    CoursePic findCoursePic(String courseId);

    @ApiOperation("课程图片删除")
    ResponseResult deleteCoursePic(String courseId);

    @ApiOperation("课程视图查询")
    CourseView courseView(String id);

    @ApiOperation("预览课程")
    CoursePublishResult preview(String id);

    @ApiOperation("发布课程")
    CoursePublishResult publish( String id);

    @ApiOperation("保存媒资信息")
    ResponseResult savemedia(TeachplanMedia teachplanMedia);
}
