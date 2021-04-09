package com.xuecheng.manage_cms.dao;

import com.xuecheng.framework.domain.cms.CmsConfig;
import com.xuecheng.framework.domain.cms.CmsTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * 配置管理信息持久层接口  继承MongoRepository  使用Spring Data Mongodb完成Mongodb数据库的查询
 */
public interface CmsTemplateRepository extends MongoRepository<CmsTemplate,String> {

}
