package com.xuecheng.learning.mq;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.learning.config.RabbitMQConfig;
import com.xuecheng.learning.service.LearningService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@Component
public class ChooseCourseTask {

    @Autowired
    LearningService learningService;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = {RabbitMQConfig.XC_LEARNING_ADDCHOOSECOURSE})
    public void receiveChoosecourseTask(XcTask xcTask){
        //取出消息的内容
        String requestBody = xcTask.getRequestBody();
        Map map = JSON.parseObject(requestBody, Map.class);
        String userId = map.get("userId").toString();
        String courseId = map.get("courseId").toString();
        //String valid = map.get("valid").toString();
        Date startTime = null;
        Date endTime = null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (map.get("startTime") != null){
            try {
                startTime =  simpleDateFormat.parse((String) map.get("startTime"));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if (map.get("endTime") != null){
            try {
                endTime =  simpleDateFormat.parse((String) map.get("endTime"));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        //添加选课
        //String userId, String courseId, String valid, Date startTime, Date endTime, XcTask xcTask
        ResponseResult addcourse = learningService.addcourse(userId, courseId, null, startTime, endTime, xcTask);
        if (addcourse.isSuccess()){
            //发送完成选课消息
            rabbitTemplate.convertAndSend(RabbitMQConfig.EX_LEARNING_ADDCHOOSECOURSE,RabbitMQConfig.XC_LEARNING_FINISHADDCHOOSECOURSE_KEY,xcTask);
        }

    }
}
