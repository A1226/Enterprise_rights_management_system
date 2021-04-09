package com.xuecheng.order.mq;

import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.order.config.RabbitMQConfig;
import com.xuecheng.order.service.TaskService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Spring Task 串行任务
 */
@Component
public class ChooseCourseTask {

    @Autowired
    TaskService taskService;

    private static final Logger LOGGER = LoggerFactory.getLogger(ChooseCourseTask.class);

    @RabbitListener(queues = RabbitMQConfig.XC_LEARNING_FINISHADDCHOOSECOURSE)
    public void receiveFinishChoosecourseTask(XcTask xcTask){
        if (xcTask != null && StringUtils.isNotEmpty(xcTask.getId())){
            taskService.finishTask(xcTask.getId());
        }
    }

    @Scheduled(cron = "0/3 * * * * *")
    public void sendChoosecourseTask(){
        //取出当前时间1分钟之前的时间
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date());
        calendar.add(GregorianCalendar.MINUTE,-1);
        Date time = calendar.getTime();
        List<XcTask> taskList = taskService.findTaskList(time, 1000);
        System.out.println(taskList);
        //调用service发布消息，将添加选课任务发送给mq
        for (XcTask xcTask : taskList) {
            //取任务
            if (taskService.getTask(xcTask.getId(),xcTask.getVersion())>0){
                String exchange = xcTask.getMqExchange();
                String routingkey = xcTask.getMqRoutingkey();
                taskService.publish(xcTask,exchange,routingkey);
            }
        }
    }

//    @Scheduled(fixedRate = 3000) //上传执行开始3秒后执行
    public void  taskTest(){
        LOGGER.info("--------------任务1开始--------------");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LOGGER.info("--------------任务1结束--------------");
    }

//    @Scheduled(fixedRate = 3000) //上传执行开始3秒后执行
    public void  taskTest1(){
        LOGGER.info("--------------任务2开始--------------");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LOGGER.info("--------------任务2结束--------------");
    }
}
