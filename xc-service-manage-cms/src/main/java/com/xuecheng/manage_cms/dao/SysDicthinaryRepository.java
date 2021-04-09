package com.xuecheng.manage_cms.dao;

import com.xuecheng.framework.domain.system.SysDictionary;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * 数据字典持久层接口
 */
public interface SysDicthinaryRepository extends MongoRepository<SysDictionary,String> {

    //课程等级查询
    SysDictionary findBydType(String dType);
}
