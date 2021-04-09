package com.xuecheng.manage_course.service;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.domain.course.*;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.ext.CourseView;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.domain.course.response.AddCourseResult;
import com.xuecheng.framework.domain.course.response.CourseCode;
import com.xuecheng.framework.domain.course.response.CoursePublishResult;
import com.xuecheng.framework.domain.course.response.TeachplanMediaPub;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_course.client.CmsPageClient;
import com.xuecheng.manage_course.dao.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class CourseService {

    @Autowired
    TeachplanMapper teachplanMapper;

    @Autowired
    TeachplanRepository teachplanRepository;

    @Autowired
    CourseBaseRepository courseBaseRepository;

    @Autowired
    CourseMapper courseMapper;

    @Autowired
    CourseBaseRepository baseRepository;

    @Autowired
    CourseMarketRepository courseMarketRepository;

    @Autowired
    CoursePicRepository coursePicRepository;

    @Autowired
    CmsPageClient cmsPageClient;

    @Autowired
    CoursePubRepository coursePubRepository;

    @Autowired
    TeachplanMediaRepository teachplanMediaRepository;

    @Autowired
    TeachplanMediaPubRepository teachplanMediaPubRepository;

    @Value("${course-publish.dataUrlPre}")
    private String publish_dataUrlPre;
    @Value("${course-publish.pagePhysicalPath}")
    private String publish_page_physicalpath;
    @Value("${course-publish.pageWebPath}")
    private String publish_page_webpath;
    @Value("${course-publish.siteId}")
    private String publish_siteId;
    @Value("${course-publish.templateId}")
    private String publish_templateId;
    @Value("${course-publish.previewUrl}")
    private String previewUrl;

    /**
     * 查询课程计划
     * @param courseId
     * @return
     */
    public TeachplanNode findTeachplanList(String courseId){
        TeachplanNode teachplanNode = teachplanMapper.selectList(courseId);
        return teachplanNode;
    }

    /**
     * 新增课程计划
     * @param teachplan
     * @return
     */
    //添加课程计划
    @Transactional
    public ResponseResult addTeachplan(Teachplan teachplan) {
        if(teachplan == null ||
                StringUtils.isEmpty(teachplan.getCourseid()) ||
                StringUtils.isEmpty(teachplan.getPname())){
            ExceptionCast.cast(CommonCode.INVALIDPARAM);
        }
        //课程id
        String courseid = teachplan.getCourseid();
        //页面传入的parentId
        String parentid = teachplan.getParentid();
        if(StringUtils.isEmpty(parentid)){
            //取出该课程的根结点
            parentid = this.getTeachplanRoot(courseid);
        }
        Optional<Teachplan> optional = teachplanRepository.findById(parentid);
        Teachplan parentNode = optional.get();
        //父结点的级别
        String grade = parentNode.getGrade();
        //新结点
        Teachplan teachplanNew = new Teachplan();
        //将页面提交的teachplan信息拷贝到teachplanNew对象中
        BeanUtils.copyProperties(teachplan,teachplanNew);
        teachplanNew.setParentid(parentid);
        teachplanNew.setCourseid(courseid);
        if(grade.equals("1")){
            teachplanNew.setGrade("2");//级别，根据父结点的级别来设置
        }else{
            teachplanNew.setGrade("3");
        }

        teachplanRepository.save(teachplanNew);

        return new ResponseResult(CommonCode.SUCCESS);
    }

    //查询课程的根结点，如果查询不到要自动添加根结点
    private String getTeachplanRoot(String courseId){
        Optional<CourseBase> optional = courseBaseRepository.findById(courseId);
        if(!optional.isPresent()){
            return null;
        }
        //课程信息
        CourseBase courseBase = optional.get();
        //查询课程的根结点
        List<Teachplan> teachplanList = teachplanRepository.findByCourseidAndParentid(courseId, "0");
        if(teachplanList == null || teachplanList.size()<=0){
            //查询不到，要自动添加根结点
            Teachplan teachplan = new Teachplan();
            teachplan.setParentid("0");
            teachplan.setGrade("1");
            teachplan.setPname(courseBase.getName());
            teachplan.setCourseid(courseId);
            teachplan.setStatus("0");
            teachplanRepository.save(teachplan);
            return teachplan.getId();
        }
        //返回根结点id
        return teachplanList.get(0).getId();
    }

    //课程列表分页查询
    public QueryResponseResult findCourseList( int page, int size, CourseListRequest courseListRequest,String companyId){
        if (courseListRequest == null){
            courseListRequest = new CourseListRequest();
        }
        if (page < 0){
            page = 0;
        }
        if (size < 0){
            size = 10;
        }
        //设置分页参数
        PageHelper.startPage(page,size);
        //公司ID
        courseListRequest.setCompanyId(companyId);
        //分页查询
        Page<CourseInfo> courseListPage = courseMapper.findCourseListPage(courseListRequest);
        //查询列表
        List<CourseInfo> result = courseListPage.getResult();
        //总记录数
        long total = courseListPage.getTotal();

        //查询结果集
        QueryResult<CourseInfo> queryResult = new QueryResult<>();
        queryResult.setList(result);
        queryResult.setTotal(total);

        return new QueryResponseResult(CommonCode.SUCCESS,queryResult);
    }

    //添加课程提交
    @Transactional
    public AddCourseResult addCourseBase(CourseBase courseBase){
        courseBase.setStatus("202001");
        baseRepository.save(courseBase);
        return new AddCourseResult(CommonCode.SUCCESS,courseBase.getId());
    }

    //课程基本信息回显
    public CourseBase getCourseBaseById(String courseId){
        return courseMapper.findCourseBaseById(courseId);
    }

    //课程基本信息更新
    @Transactional
    public ResponseResult UpdateCourseBase(String courseId, CourseBase courseBase){
        CourseBase one = this.getCourseBaseById(courseId);
        if (one == null){
            ExceptionCast.cast(CommonCode.INVALIDPARAM);
        }
        one.setName(courseBase.getName());
        one.setMt(courseBase.getMt());
        one.setSt(courseBase.getSt());
        one.setGrade(courseBase.getGrade());
        one.setStudymodel(courseBase.getStudymodel());
        one.setUsers(courseBase.getUsers());
        one.setDescription(courseBase.getDescription());
        courseBaseRepository.save(one);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    //课程营销查询
    public CourseMarket getCourseMarketById(String courseId){
        Optional<CourseMarket> optional = courseMarketRepository.findById(courseId);
        if (optional.isPresent()){
            return optional.get();
        }
        return null;
    }

    //课程营销更新
    public CourseMarket UpdateCourseMarket(String courseId,CourseMarket courseMarket){
        CourseMarket market = this.getCourseMarketById(courseId);
        if (market != null){
            market.setCharge(courseMarket.getCharge());
            market.setValid(courseMarket.getValid());
            market.setQq(courseMarket.getQq());
            market.setPrice(courseMarket.getPrice());
            market.setStartTime(courseMarket.getStartTime());
            market.setEndTime(courseMarket.getEndTime());
            courseMarketRepository.save(market);
        }else {
            market = new CourseMarket();
            //将得到的数据映射到market中 （字段必须要有）
            BeanUtils.copyProperties(courseMarket,market);
            courseMarketRepository.save(market);
        }
        return market;
    }

    //添加课程图片
    @Transactional
    public ResponseResult saveCoursePic(String courseId,String pic){
        //查询课程图片
        Optional<CoursePic> optional = coursePicRepository.findById(courseId);
        CoursePic coursePic = null;
        if (optional.isPresent()){
            coursePic = optional.get();
        }
        //如果没有则新创建
        if (coursePic == null){
            coursePic = new CoursePic();
        }
        coursePic.setCourseid(courseId);
        coursePic.setPic(pic);
        coursePicRepository.save(coursePic);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    //课程图片查询
    public CoursePic findCoursePic(String courseId){
        Optional<CoursePic> optionalPic = coursePicRepository.findById(courseId);
        if (optionalPic.isPresent()){
            return optionalPic.get();
        }
        return null;
    }

    //课程图片删除
    @Transactional
    public ResponseResult deleteCoursePic(String courseId){
        long deleteByCourseId = coursePicRepository.deleteByCourseid(courseId);
        if (deleteByCourseId>0){
            return new ResponseResult(CommonCode.SUCCESS);
        }
        return new ResponseResult(CommonCode.FAIL);
    }

    //课程视图查询
    public CourseView getCourseView(String id){
        CourseView courseView = new CourseView();
        //课程基本信息
        Optional<CourseBase> optionalBase = courseBaseRepository.findById(id);
        if (optionalBase.isPresent()){
            CourseBase courseBase = optionalBase.get();
            courseView.setCourseBase(courseBase);
        }
        //课程营销
        Optional<CourseMarket> optionalMarket = courseMarketRepository.findById(id);
        if (optionalMarket.isPresent()){
            CourseMarket courseMarket = optionalMarket.get();
            courseView.setCourseMarket(courseMarket);
        }
        //课程图片
        Optional<CoursePic> coursePic = coursePicRepository.findById(id);
        if (coursePic.isPresent()){
            CoursePic getCoursePic = coursePic.get();
            courseView.setCoursePic(getCoursePic);
        }
        //课程计划信息
        TeachplanNode teachplanNode = teachplanMapper.selectList(id);
        courseView.setTeachplanNode(teachplanNode);
        return courseView;
    }

    //根据id查询课程基本信息
    public CourseBase findCourseBaseById(String courseId){
        Optional<CourseBase> baseOptional = courseBaseRepository.findById(courseId);
        if(baseOptional.isPresent()){
            CourseBase courseBase = baseOptional.get();
            return courseBase;
        }
        ExceptionCast.cast(CourseCode.COURSE_GET_NOTEXISTS);
        return null;
    }

    //课程预览
    public CoursePublishResult preview(String id) {
        CourseBase one = this.findCourseBaseById(id);
        //请求cms添加页面
        //准备cmsPage信息
        CmsPage cmsPage = new CmsPage();
        cmsPage.setSiteId(publish_siteId);//站点
        cmsPage.setTemplateId(publish_templateId);//模板
        cmsPage.setDataUrl(publish_dataUrlPre+id);//数据URL
        cmsPage.setPageWebPath(publish_page_webpath);//页面访问路径
        cmsPage.setPagePhysicalPath(publish_page_physicalpath);//页面存储路径
        cmsPage.setPageAliase(one.getName());//页面别名
        cmsPage.setPageName(id+".html");


        //远程调用cms
        CmsPageResult cmsPageResult = cmsPageClient.saveCmaPage(cmsPage);
        if (!cmsPageResult.isSuccess()){
            //抛出异常
            return new CoursePublishResult(CommonCode.FAIL,null);
        }
        CmsPage page = cmsPageResult.getCmsPage();
        String pageId = page.getPageId();
        //拼装页面预览的URL
        String pageUrl = previewUrl+pageId;
        return new CoursePublishResult(CommonCode.SUCCESS,pageUrl);
    }

    //课程发布
    @Transactional
    public CoursePublishResult publish(String id) {
        //课程信息
        CourseBase one = this.findCourseBaseById(id);
        //准备cmsPage信息
        CmsPage cmsPage = new CmsPage();
        cmsPage.setSiteId(publish_siteId);//站点
        cmsPage.setTemplateId(publish_templateId);//模板
        cmsPage.setDataUrl(publish_dataUrlPre+id);//数据URL
        cmsPage.setPageWebPath(publish_page_webpath);//页面访问路径
        cmsPage.setPagePhysicalPath(publish_page_physicalpath);//页面存储路径
        cmsPage.setPageAliase(one.getName());//页面别名
        cmsPage.setPageName(id+".html");
        //调用cms一键发布接口将课程详情页面发布到服务器
        CmsPostPageResult cmsPostPageResult = cmsPageClient.postPageQuick(cmsPage);
        if (!cmsPostPageResult.isSuccess()){
            ExceptionCast.cast(CommonCode.FAIL);
        }
        //保存课程发布状态为“已发布”
        CourseBase courseBase = this.saveCoursePubState(id);
        if (courseBase == null){
            return new  CoursePublishResult(CommonCode.FAIL,null);
        }
        //保存课程索引信息
        //先创建一个CoursePub对象查询出该信息
        CoursePub coursePub = createCoursePub(id);
        //将CoursePub对象保存到数据库
        CoursePub coursePub1 = saveCoursePub(id, coursePub);

        //缓存课程的信息

        //得到页面URL
        String pageUrl = cmsPostPageResult.getPageUrl();

        //向teachplanMediaPub中保存课程媒资信息
        saveTeachplanMediaPub(id);

        return new  CoursePublishResult(CommonCode.SUCCESS,pageUrl);
    }

    //向teachplanMediaPub中保存课程媒资信息
    private void saveTeachplanMediaPub(String courseId){
        //先查询课程媒资管理信息
        List<TeachplanMedia> byCourseId = teachplanMediaRepository.findByCourseId(courseId);
        //先删除TeachplanMediaPub的信息数据
        teachplanMediaPubRepository.deleteByCourseId(courseId);
        List<TeachplanMediaPub> mediaPubList = new ArrayList<>();
        //将课程媒资信息插入到TeachplanMediaPub
        for (TeachplanMedia media : byCourseId) {
            TeachplanMediaPub teachplanMediaPub = new TeachplanMediaPub();
            BeanUtils.copyProperties(media,teachplanMediaPub);
            //添加时间戳
            teachplanMediaPub.setTimestamp(new Date());
            mediaPubList.add(teachplanMediaPub);
        }

        //将课程媒资信息插入到TeachplanMediaPub
        teachplanMediaPubRepository.saveAll(mediaPubList);

    }
    //将CoursePub对象保存到数据库

    private CoursePub saveCoursePub(String id,CoursePub coursePub){
        CoursePub coursePubNew = null;
        //先根据ID查询
        Optional<CoursePub> optionalPub = coursePubRepository.findById(id);
        if (optionalPub.isPresent()){
            coursePubNew = optionalPub.get();
        }else {
            coursePubNew = new CoursePub();
        }
        //如果等于空就将coursePub对象的信息保存到数据库
        BeanUtils.copyProperties(coursePub,coursePubNew);
        coursePubNew.setId(id);
        //时间戳，logstash使用
        coursePubNew.setTimestamp(new Date());
        //发布时间
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String format = simpleDateFormat.format(new Date());
        coursePubNew.setPubTime(format);

        //保存
        coursePubRepository.save(coursePubNew);
        return coursePubNew;
    }

    //创建coursePub对象
    private CoursePub createCoursePub(String id){
        CoursePub coursePub = new CoursePub();
        //查询课程信息
        Optional<CourseBase> optionalCourseBase = courseBaseRepository.findById(id);
        if (optionalCourseBase.isPresent()){
            CourseBase courseBase = optionalCourseBase.get();
            BeanUtils.copyProperties(courseBase,coursePub);
        }

        //查询课程图片
        Optional<CoursePic> optionalPic = coursePicRepository.findById(id);
        if (optionalPic.isPresent()){
            CoursePic coursePic = optionalPic.get();
            BeanUtils.copyProperties(coursePic,coursePub);
        }

        //查询课程营销
        Optional<CourseMarket> optionalMarket = courseMarketRepository.findById(id);
        if (optionalMarket.isPresent()){
            CourseMarket courseMarket = optionalMarket.get();
            BeanUtils.copyProperties(courseMarket,coursePub);
        }

        //查询课程计划
        TeachplanNode teachplanNode = teachplanMapper.selectList(id);
        String teach = JSON.toJSONString(teachplanNode);
        coursePub.setTeachplan(teach);

        return coursePub;
    }

    //更新课程发布状态
    private CourseBase saveCoursePubState(String courseId){
        CourseBase courseBase = this.findCourseBaseById(courseId);
        //更新发布状态
        courseBase.setStatus("202002");
        CourseBase save = courseBaseRepository.save(courseBase);
        return save;
    }

    //保存课程计划与媒资管理信息
    @Transactional
    public ResponseResult savemedia(TeachplanMedia teachplanMedia) {
        if (teachplanMedia == null || StringUtils.isEmpty(teachplanMedia.getTeachplanId())){
            ExceptionCast.cast(CommonCode.INVALIDPARAM);
        }

        //课程计划ID
        String teachplanId = teachplanMedia.getTeachplanId();

        //查询课程计划
        Optional<Teachplan> optional = teachplanRepository.findById(teachplanId);
        if (!optional.isPresent()){
            ExceptionCast.cast(CourseCode.COURSE_MEDIA_TEACHPLAN_ISNULL);
        }
        //查询的信息
        Teachplan teachplan = optional.get();
        //取出课程计划等级
        String grade = teachplan.getGrade();

        if (StringUtils.isEmpty(grade) || !grade.equals("3")){
            ExceptionCast.cast(CourseCode.COURSE_MEDIA_TEACHPLAN_GRADEERROR);
        }

        TeachplanMedia media = null;
        Optional<TeachplanMedia> teachplanMediaOptional = teachplanMediaRepository.findById(teachplanId);
        if (!teachplanMediaOptional.isPresent()){
            media = new TeachplanMedia();
        }else {
            media = teachplanMediaOptional.get();
        }
        //保存或更新信息
        media.setTeachplanId(teachplanId);
        media.setCourseId(teachplanMedia.getCourseId());
        media.setMediaFileOriginalName(teachplanMedia.getMediaFileOriginalName());
        media.setMediaId(teachplanMedia.getMediaId());
        media.setMediaUrl(teachplanMedia.getMediaUrl());

        teachplanMediaRepository.save(media);

        return new ResponseResult(CommonCode.SUCCESS);

    }
}
