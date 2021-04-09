package com.xuecheng.manage_cms.dao;

import com.xuecheng.framework.domain.cms.CmsPage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Optional;

@SpringBootTest
@RunWith(SpringRunner.class)
public class CmsPageRepositoryTest {

    @Autowired
    CmsPageRepository cmsPageRepository;

    /**
     * 测试MongoDB
     */
    @Test
    public void testFindAll(){
        List<CmsPage> all = cmsPageRepository.findAll();
        System.out.println(all);
    }

    /**
     * 分页测试
     */
    @Test
    public void testFindAllPage(){
        //分页参数
        int page = 0;
        int size = 10;
        //需要Pageable对象
        Pageable pageable = PageRequest.of(page,size);
        Page<CmsPage> all = cmsPageRepository.findAll(pageable);
        System.out.println(all);
    }

    /**
     * 更新
     */
    @Test
    public void updateMongo(){
        //先查询
        Optional<CmsPage> optional = cmsPageRepository.findById("5abefd525b05aa293098fca6");
        CmsPage cmsPage = optional.get();

        //更新的数据
        cmsPage.setPageAliase("ccc");

        //保存
        CmsPage save = cmsPageRepository.save(cmsPage);
        System.out.println(save);
    }

    @Test
    public void findAllTest(){

        //分页参数
        int page = 0;
        int size = 10;
        Pageable pageable = PageRequest.of(page,size);
//        Page<CmsPage> pages = cmsPageRepository.findAll(pageable);

        //定义条件值
        CmsPage cmsPage = new CmsPage();
//        cmsPage.setTemplateId("5ad9a24d68db5239b8fef199");
        cmsPage.setPageAliase("预览");

        //条件匹配器  包含该字段的关键字
        ExampleMatcher exampleMatcher = ExampleMatcher.matching()
            .withMatcher("pageAliase",ExampleMatcher.GenericPropertyMatchers.contains());

        //定义Example
        Example<CmsPage> example = Example.of(cmsPage,exampleMatcher);
        Page<CmsPage> all = cmsPageRepository.findAll(example, pageable);
        List<CmsPage> content = all.getContent();
        System.out.println(content);


    }
}
