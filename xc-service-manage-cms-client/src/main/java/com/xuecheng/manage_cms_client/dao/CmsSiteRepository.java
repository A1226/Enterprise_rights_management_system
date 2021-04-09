package com.xuecheng.manage_cms_client.dao;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsSite;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * 分页查询持久层接口  继承MongoRepository  使用Spring Data Mongodb完成Mongodb数据库的查询
 */
public interface CmsSiteRepository extends MongoRepository<CmsSite,String> {

}
