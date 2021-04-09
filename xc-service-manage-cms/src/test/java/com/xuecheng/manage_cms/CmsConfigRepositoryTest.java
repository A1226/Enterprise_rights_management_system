package com.xuecheng.manage_cms;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.system.SysDictionary;
import com.xuecheng.manage_cms.dao.CmsPageRepository;
import com.xuecheng.manage_cms.dao.SysDicthinaryRepository;
import com.xuecheng.manage_cms.service.PageService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@SpringBootTest
@RunWith(SpringRunner.class)
public class CmsConfigRepositoryTest {

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    PageService pageService;

    @Autowired
    SysDicthinaryRepository sysDicthinaryRepository;

    /**
     * 测试RestTemplate
     */
    @Test
    public void restTemplate(){
        ResponseEntity<Map> forEntity = restTemplate.getForEntity("http://localhost:31001/cms/config/getmodel/5a791725dd573c3574ee333f", Map.class);
        System.out.println(forEntity);
    }

    @Test
    public void getPageHtmlTest(){
        String pageHtml = pageService.getPageHtml("603bab57434eb30810480f4d");
        System.out.println(pageHtml);
    }

    @Test
    public void getType(){
        SysDictionary bydType = sysDicthinaryRepository.findBydType("200");
        System.out.println(bydType);
    }

}
