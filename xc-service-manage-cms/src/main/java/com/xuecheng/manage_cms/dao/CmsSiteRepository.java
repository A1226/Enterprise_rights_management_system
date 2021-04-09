package com.xuecheng.manage_cms.dao;

import com.xuecheng.framework.domain.cms.CmsConfig;
import com.xuecheng.framework.domain.cms.CmsSite;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * 配置管理信息持久层接口  继承MongoRepository  使用Spring Data Mongodb完成Mongodb数据库的查询
 * 接口中需要获取站点的信息（站点域名、站点访问路径等）
 */
public interface CmsSiteRepository extends MongoRepository<CmsSite,String> {

}
