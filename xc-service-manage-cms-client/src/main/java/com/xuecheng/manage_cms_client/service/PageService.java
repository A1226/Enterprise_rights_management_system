package com.xuecheng.manage_cms_client.service;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.gridfs.GridFS;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsSite;
import com.xuecheng.manage_cms_client.dao.CmsPageRepository;
import com.xuecheng.manage_cms_client.dao.CmsSiteRepository;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Optional;

@Service
public class PageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PageService.class);

    @Autowired
    GridFsTemplate gridFsTemplate;

    @Autowired
    GridFSBucket gridFSBucket;

    @Autowired
    CmsPageRepository cmsPageRepository;

    @Autowired
    CmsSiteRepository cmsSiteRepository;

    //将页面html保存到页面物理路径
    public void savePageToServerPath(String pageId){
        //根据pageID查询CmsPage
        CmsPage pageById = this.findCmsPageById(pageId);
        //得到HTML的文件ID，从cmsPage中获取HtmlFileId内容
        String htmlFileId = pageById.getHtmlFileId();

        //从girdFS中查询HTML文件
        InputStream inputStream = this.getFileById(htmlFileId);
        if (inputStream == null){
            LOGGER.error("getFileById inputStream is null htmlFileId:{"+htmlFileId+"}");
            return;
        }
        //得到站点的ID
        String siteId = pageById.getSiteId();
        //得到站点的信息
        CmsSite cmsSiteById = this.findCmsSiteById(siteId);
        //得到站点的物理路径
        String sitePhysicalPath = cmsSiteById.getSitePhysicalPath();
        //得到页面的物理路径
        String pagePath = sitePhysicalPath + pageById.getPagePhysicalPath() + pageById.getPageName();
        //将HTML文件保存到服务器物理路径上
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(new File(pagePath));
            IOUtils.copy(inputStream,fileOutputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                inputStream.close();
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //根据文件ID从GriFS中查询文件内容
    public InputStream getFileById(String fileId){
        //文件对象
        GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(fileId)));
        //打开下载流
        GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
        //定义GridFsResource
        GridFsResource gridFsResource = new GridFsResource(gridFSFile,gridFSDownloadStream);
        try {
            return gridFsResource.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  null;
    }

    //根据pageID查询CmsPage信息
    public CmsPage findCmsPageById(String pageId){
        Optional<CmsPage> cmsPage = cmsPageRepository.findById(pageId);
        if (cmsPage.isPresent()){
            return cmsPage.get();
        }
        return null;
    }
    //根据站点ID查询站点信息
    public CmsSite findCmsSiteById(String siteId){
        Optional<CmsSite> cmsSite = cmsSiteRepository.findById(siteId);
        if (cmsSite.isPresent()){
            return cmsSite.get();
        }
        return null;
    }
}
