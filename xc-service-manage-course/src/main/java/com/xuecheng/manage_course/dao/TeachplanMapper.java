package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import org.apache.ibatis.annotations.Mapper;

/**
 * 课程计划查询持久层
 */

@Mapper
public interface TeachplanMapper {
    TeachplanNode selectList(String courseId);
}
