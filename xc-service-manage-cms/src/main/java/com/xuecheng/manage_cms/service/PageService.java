package com.xuecheng.manage_cms.service;

import com.alibaba.fastjson.JSON;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsSite;
import com.xuecheng.framework.domain.cms.CmsTemplate;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsCode;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.*;
import com.xuecheng.manage_cms.config.RabbitmqConfig;
import com.xuecheng.manage_cms.dao.CmsPageRepository;
import com.xuecheng.manage_cms.dao.CmsSiteRepository;
import com.xuecheng.manage_cms.dao.CmsTemplateRepository;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


/**
 * cms业务层开发
 */
@Service
public class PageService {

    @Autowired
    CmsPageRepository cmsPageRepository;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    CmsTemplateRepository cmsTemplateRepository;

    @Autowired
    GridFsTemplate gridFsTemplate;

    @Autowired
    GridFSBucket gridFSBucket;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    CmsSiteRepository cmsSiteRepository;

    /**
     * 分页带条件查询信息
     * @param page 当前页码
     * @param size 每页数量
     * @param queryPageRequest 条件
     * @return
     */
    public QueryResponseResult findList(int page, int size, QueryPageRequest queryPageRequest){
        //防止空指针异常
        if (queryPageRequest == null){
            queryPageRequest = new QueryPageRequest();
        }

        //自定义条件查询
        //定义条件匹配器
        ExampleMatcher exampleMatcher = ExampleMatcher.matching()
            .withMatcher("pageAliase",ExampleMatcher.GenericPropertyMatchers.contains());
        //条件值对象
        CmsPage cmsPage = new CmsPage();
        //设置站点ID
        if (StringUtils.isNotEmpty(queryPageRequest.getSiteId())){
            cmsPage.setSiteId(queryPageRequest.getSiteId());
        }
        //设置模板ID
        if (StringUtils.isNotEmpty(queryPageRequest.getTemplateId())){
            cmsPage.setTemplateId(queryPageRequest.getTemplateId());
        }
        //设置页面别名
        if (StringUtils.isNotEmpty(queryPageRequest.getPageAliase())){
            cmsPage.setPageAliase(queryPageRequest.getPageAliase());
        }
        //定义条件实例
        Example<CmsPage> example = Example.of(cmsPage,exampleMatcher);

        //分页参数
        if (page <= 0){
            page = 1;
        }
        page = page - 1;
        if (size <= 0){
            size = 10;
        }
        //需要Pageable对象 需要分页参数
        Pageable pageable = PageRequest.of(page,size);
        Page<CmsPage> all = cmsPageRepository.findAll(example,pageable);//定义查询条件并且分页
        QueryResult<CmsPage> queryResult = new QueryResult();
        queryResult.setList(all.getContent());//数据列表
        queryResult.setTotal(all.getTotalElements());//数据总记录数
        QueryResponseResult queryResponseResult = new QueryResponseResult(CommonCode.SUCCESS,queryResult);
        return queryResponseResult;
    }

    /**
     * 新增页面
     * @param cmsPage 新增信息对象
     * @return
     */
    public CmsPageResult add(CmsPage cmsPage){
        if (cmsPage == null){
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAISNULL);
        }
        //校验页面是否存在，根据页面名称、站点Id、页面webpath查询
        CmsPage path = cmsPageRepository.findByPageNameAndSiteIdAndPageWebPath(cmsPage.getPageName(), cmsPage.getSiteId(), cmsPage.getPageWebPath());
        //校验页面存在，返回失败指令
        if (path != null){
            ExceptionCast.cast(CmsCode.CMS_ADDPAGE_EXISTSNAME);
        }
        //调用dao来保存
        //防止插入值把主键设置为空，让MongoDB自动生成
        cmsPage.setPageId(null);
        cmsPageRepository.save(cmsPage);
        //新增成功，返回成功指令和对象
        return new CmsPageResult(CommonCode.SUCCESS,cmsPage);



    }

    /**
     * 根据ID查询
     * @param id pageID
     * @return
     */
    public CmsPage findById(String id){
        //根据ID查询相关信息
        Optional<CmsPage> pageId = cmsPageRepository.findById(id);
        //如果Optional里有数据就返回，没有返回null
        if (pageId.isPresent()){
            return pageId.get();
        }
        return null;
    }

    /**
     * 根据ID更新信息
     * @param id
     * @param cmsPage
     * @return
     */
    public CmsPageResult update(String id,CmsPage cmsPage){
        //根据ID查询相关信息
        CmsPage page = this.findById(id);
        if (page != null){
            //更新所属站点
            page.setSiteId(cmsPage.getSiteId());
            ////更新页面别名
            page.setPageAliase(cmsPage.getPageAliase());
            //更新页面名称
            page.setPageName(cmsPage.getPageName());
            ////更新模板id
            page.setTemplateId(cmsPage.getTemplateId());
            //更新访问路径
            page.setPageWebPath(cmsPage.getPageWebPath());
            //更新物理路径
            page.setPagePhysicalPath(cmsPage.getPagePhysicalPath());
            //更新dataURL
            page.setDataUrl(cmsPage.getDataUrl());

            //执行更新
            cmsPageRepository.save(page);

            return new CmsPageResult(CommonCode.SUCCESS,page);
        }

        //更新失败
        return new CmsPageResult(CommonCode.FAIL,null);
    }

    /**
     * 根据ID删除页面
     * @param pageId
     * @return
     */
    public ResponseResult delete(String pageId){
        //判断是否有该页面
        CmsPage one = this.findById(pageId);
        if (one != null){
            //删除页面
            cmsPageRepository.deleteById(pageId);
            return new ResponseResult(CommonCode.SUCCESS);
        }
        return new ResponseResult(CommonCode.FAIL);
    }

    //页面静态化
    public String getPageHtml(String pageId){
        //获取页面模型数据
        Map model = getModelByPageId(pageId);
        if (model == null){
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAISNULL);
        }
        //获取页面模板
        String template = getTemplateByPageId(pageId);
        if (StringUtils.isEmpty(template)){
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL);
        }
        //执行静态化
        String html = generateHtml(template, model);
        return html;
    }

    //执行静态化
    private String generateHtml(String template,Map model){
        //生成配置类
        Configuration configuration = new Configuration(Configuration.getVersion());
        //模板加载器
        StringTemplateLoader stringTemplateLoader = new StringTemplateLoader();
        stringTemplateLoader.putTemplate("template",template);
        //配置模板加载器
        configuration.setTemplateLoader(stringTemplateLoader);

        //获取模板
        try {
            Template template1 = configuration.getTemplate("template");
            String html = FreeMarkerTemplateUtils.processTemplateIntoString(template1, model);
            return html;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //获取页面模板
    private String getTemplateByPageId(String pageId){
        //查询页面信息
        CmsPage cmsPage = this.findById(pageId);
        //页面不存在 返回异常
        if (cmsPage == null){
            ExceptionCast.cast(CmsCode.CMS_ADDPAGE_EXISTSNAME);
        }
        //获取模板的ID
        String templateId = cmsPage.getTemplateId();
        if (StringUtils.isEmpty(templateId)){
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL);
        }
        //根据ID调用dao查询模板信息
        Optional<CmsTemplate> optional = cmsTemplateRepository.findById(templateId);
        if (optional.isPresent()){
            CmsTemplate cmsTemplate = optional.get();
            //获取模板文件ID
            String templateById = cmsTemplate.getTemplateFileId();
            //根据id查询文件
            GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(templateById)));
            //打开下载流对象
            GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
            //创建gridFsResource，用于获取流对象
            GridFsResource gridFsResource = new GridFsResource(gridFSFile,gridFSDownloadStream);
            //获取流中的数据
            try {
                String conten = IOUtils.toString(gridFsResource.getInputStream(), "UTF-8");
                return conten;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    //获取页面模型数据
    private Map getModelByPageId(String pageId){
        //查询页面信息
        CmsPage cmsPage = this.findById(pageId);
        //页面不存在 返回异常
        if (cmsPage == null){
            ExceptionCast.cast(CmsCode.CMS_ADDPAGE_EXISTSNAME);
        }
        //存在获取dataURL
        String dataUrl = cmsPage.getDataUrl();
        if (StringUtils.isEmpty(dataUrl)){
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAURLISNULL);
        }
        //通过restTemplate请求dataUrl获取数据
        ResponseEntity<Map> forEntity = restTemplate.getForEntity(dataUrl, Map.class);
        Map body = forEntity.getBody();
        return body;
    }

    //页面发布
    public ResponseResult post(String pageId){
        //执行页面静态化
        String pageHtml = this.getPageHtml(pageId);
        //将页面静态化文件存储到GridFs中
        CmsPage cmsPage = saveHtml(pageId, pageHtml);
        //向MQ发消息
        sendPostPage(pageId);
        return new ResponseResult((CommonCode.SUCCESS));
    }

    //向mq发送消息
    private void sendPostPage(String pageId){
        //先得到页面信息
        CmsPage cmsPage = this.findById(pageId);
        if (cmsPage == null){
            ExceptionCast.cast(CommonCode.INVALIDPARAM);
        }
        //创建消息对象
        Map<String,String> map = new HashMap<>();
        map.put("pageId",pageId);
        //转为json
        String jsonString = JSON.toJSONString(map);
        //站点ID
        String siteId = cmsPage.getSiteId();
        //发送mq
        rabbitTemplate.convertAndSend(RabbitmqConfig.EX_ROUTING_CMS_POSTPAGE,siteId,jsonString);
    }

    //保存HTML到GridFs中
    private CmsPage saveHtml(String pageId,String htmlContent){
        //先得到页面信息
        CmsPage cmsPage = this.findById(pageId);
        if (cmsPage == null){
            ExceptionCast.cast(CommonCode.INVALIDPARAM);
        }
        ObjectId objectId = null;
        try {
            //将htmlContent内容转成输入流
            InputStream inputStream = IOUtils.toInputStream(htmlContent, "UTF-8");
            //将HTML文件内容保存到GridFS
            objectId = gridFsTemplate.store(inputStream, cmsPage.getPageName());
        } catch (IOException e) {
            e.printStackTrace();
        }

        //将HTML文件ID更新到cmsPage中
        cmsPage.setHtmlFileId(objectId.toHexString());
        cmsPageRepository.save(cmsPage);
        return cmsPage;
    }

    //页面保存  如何页面存在就更新，不存在则添加
    public CmsPageResult save(CmsPage cmsPage) {
        CmsPage page = cmsPageRepository.findByPageNameAndSiteIdAndPageWebPath(cmsPage.getPageName(), cmsPage.getSiteId(), cmsPage.getPageWebPath());
        if (page != null){
            return this.update(page.getPageId(), cmsPage);
        }else {
            return this.add(cmsPage);
        }

    }

    //根据站点ID查询该站点信息
    public CmsSite findByCmsSiteId(String siteId){
        Optional<CmsSite> optionalSite = cmsSiteRepository.findById(siteId);
        if (optionalSite.isPresent()){
            CmsSite cmsSite = optionalSite.get();
            return cmsSite;
        }
        return null;
    }

    //页面一键发布
    public CmsPostPageResult postPageQuick(CmsPage cmsPage) {
        //添加页面 将页面信息存储到CMSPage
        CmsPageResult save = this.save(cmsPage);
        if (!save.isSuccess()){
//            return new CmsPostPageResult(CommonCode.FAIL,null);
            ExceptionCast.cast(CommonCode.FAIL);
        }
        //得到页面ID
        CmsPage saveCmsPage = save.getCmsPage();
        String pageId = save.getCmsPage().getPageId();
        //执行页面发布（先静态化，保存GridFS，向MQ发信息）
        ResponseResult post = this.post(pageId);
        if (!post.isSuccess()){
            ExceptionCast.cast(CommonCode.FAIL);
        }
        //页面Url= cmsSite.siteDomain+cmsSite.siteWebPath+ cmsPage.pageWebPath + cmsPage.pageName
        //获取站点ID
        String siteId = saveCmsPage.getSiteId();
        //根据站点ID查询该站点信息
        CmsSite byCmsSiteId = this.findByCmsSiteId(siteId);
        //站点域名
        String siteDomain = byCmsSiteId.getSiteDomain();
        //站点web路径
        String siteWebPath = byCmsSiteId.getSiteWebPath();
        //页面web路径
        //页面名称
        //页面的访问路径
        String URL = siteDomain + siteWebPath + saveCmsPage.getPageWebPath() + saveCmsPage.getPageName();
        return new CmsPostPageResult(CommonCode.SUCCESS,URL);
    }
}
