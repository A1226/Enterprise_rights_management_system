package com.xuecheng.search;

import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestIndex {

    @Autowired
    RestHighLevelClient client;

    @Autowired
    RestClient restClient;

    //删除索引
    @Test
    public void deleteIndex() throws IOException {
        //创建删除索引对象
        DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest("xc_test");
        //操作索引的客户端
        IndicesClient indicesClient = client.indices();
        //执行删除索引
        DeleteIndexResponse delete = indicesClient.delete(deleteIndexRequest);
        boolean acknowledged = delete.isAcknowledged();
        System.out.println(acknowledged);
    }

    //创建索引
    @Test
    public void createIndex() throws IOException {
//        创建索引对象
        CreateIndexRequest createIndexRequest = new CreateIndexRequest("xc_test");
        //设置参数
        createIndexRequest.settings(Settings.builder().put("number_of_shards", "1").put("number_of_replicas", "0"));
//        创建映射
        createIndexRequest.mapping("doc","{\n" +
                "\t\"properties\": {\n" +
                "\t\t\"description\": {\n" +
                "\t\t\t\"type\": \"text\",\n" +
                "\t\t\t\"analyzer\": \"ik_max_word\",\n" +
                "\t\t\t\"search_analyzer\": \"ik_smart\"\n" +
                "\t\t},\n" +
                "\t\t\"name\": {\n" +
                "\t\t\t\"type\": \"text\",\n" +
                "\t\t\t\"analyzer\": \"ik_max_word\",\n" +
                "\t\t\t\"search_analyzer\": \"ik_smart\"\n" +
                "\t\t},\n" +
                "\t\t\"pic\":{\n" +
                "\t\t\t\"type\":\"text\",\n" +
                "\t\t\t\"index\":false\n" +
                "\t\t},\n" +
                "\t\t\"price\": {\n" +
                "\t\t\t\"type\": \"float\"\n" +
                "\t\t},\n" +
                "\t\t\"studymodel\": {\n" +
                "\t\t\t\"type\": \"keyword\"\n" +
                "\t\t},\n" +
                "\t\t\"timestamp\": {\n" +
                "\t\t\t\"type\": \"date\",\n" +
                "\t\t\t\"format\": \"yyyy‐MM‐dd HH:mm:ss||yyyy‐MM‐dd||epoch_millis\"\n" +
                "\t\t}\n" +
                "\t}\n" +
                "}", XContentType.JSON);
        //操作索引客户端
        IndicesClient indices = client.indices();
        //执行创建索引库
        CreateIndexResponse createIndexResponse = indices.create(createIndexRequest);
        //得到响应
        boolean acknowledged = createIndexResponse.isAcknowledged();
        System.out.println(acknowledged);
    }

    //添加文档
    @Test
    public void addDoc() throws IOException {
        Map<String,Object> map = new HashMap<>();
        map.put("name","spring cloud实战");
        map.put("description", "本课程主要从四个章节进行讲解： 1.微服务架构入门 2.spring cloud 基础入门 3.实战Spring Boot 4.注册中心eureka。");
        map.put("studymodel", "201001");
        SimpleDateFormat dateFormat =new SimpleDateFormat("yyyy‐MM‐dd HH:mm:ss");
        map.put("timestamp", dateFormat.format(new Date()));
        map.put("price", 5.6f);

        //索引请求对象
        IndexRequest indexRequest = new IndexRequest("xc_test","doc");
        //指定文档的内容
        indexRequest.source(map);
        //索引响应对象
        IndexResponse indices = client.index(indexRequest);
        //响应结果
        DocWriteResponse.Result result = indices.getResult();
        System.out.println(result);

    }

    //查询数据
    @Test
    public void queryIndex() throws IOException {
        GetRequest getRequest = new GetRequest("xc_test","doc","eW08RXgBGxrIwvoyWu-w");
        GetResponse documentFields = client.get(getRequest);
        Map<String, Object> sourceAsMap = documentFields.getSourceAsMap();
        System.out.println(sourceAsMap);
    }
}
