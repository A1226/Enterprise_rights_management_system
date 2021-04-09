package com.xuecheng.order.service;

import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.framework.domain.task.XcTaskHis;
import com.xuecheng.order.dao.XcTaskHisRepository;
import com.xuecheng.order.dao.XcTaskRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class TaskService {

    @Autowired
    XcTaskRepository xcTaskRepository;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    XcTaskHisRepository xcTaskHisRepository;

    //查询前N条任务
    public List<XcTask> findTaskList(Date updateTime,int size){
        //设置分页参数
        Pageable pageable = new PageRequest(0,size);
        Page<XcTask> taskAll = xcTaskRepository.findByUpdateTimeBefore(pageable, updateTime);
        List<XcTask> content = taskAll.getContent();
        return content;
    }

    //发布消息
    @Transactional
    public void publish(XcTask xcTask,String ex,String routingKey){
        Optional<XcTask> optionalTask = xcTaskRepository.findById(xcTask.getId());
        if (optionalTask.isPresent()){
            //如果存在发送消息 更新时间
            rabbitTemplate.convertAndSend(ex,routingKey,xcTask);
            XcTask one = optionalTask.get();
            xcTaskRepository.updateTaskTime(one.getId(),new Date());
        }
    }

    //获取任务
    @Transactional
    public int getTask(String id ,int version){
        int content = xcTaskRepository.updateTaskVersion(id, version);
        return content;
    }

    //完成选课任务
    @Transactional
    public void finishTask(String taskId){
        Optional<XcTask> optional = xcTaskRepository.findById(taskId);
        if (optional.isPresent()){
            XcTaskHis xcTaskHis = new XcTaskHis();
            XcTask xcTask = optional.get();
            //将任务添加到历史任务
            BeanUtils.copyProperties(xcTask,xcTaskHis);
            xcTaskHisRepository.save(xcTaskHis);
            //删除当前任务
            xcTaskRepository.delete(xcTask);
        }
    }
}
