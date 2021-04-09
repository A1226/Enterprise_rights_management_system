package com.xuecheng.manage_cms.dao;

import com.xuecheng.framework.domain.cms.CmsPage;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * 分页查询持久层接口  继承MongoRepository  使用Spring Data Mongodb完成Mongodb数据库的查询
 */
public interface CmsPageRepository extends MongoRepository<CmsPage,String> {
    //新增页面：校验页面是否存在，根据页面名称、站点Id、页面webpath查询
    CmsPage findByPageNameAndSiteIdAndPageWebPath(String PageName,String SiteId,String PageWebPath);

}
