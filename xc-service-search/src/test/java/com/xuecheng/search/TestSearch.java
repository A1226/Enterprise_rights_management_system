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
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.swing.text.Highlighter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestSearch {

    @Autowired
    RestHighLevelClient client;

    @Autowired
    RestClient restClient;


    //查询全部
    @Test
    public void testSearchAll() throws IOException, ParseException {
        //搜索请求对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //指定类型
        searchRequest.types("doc");
        //搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //搜索方式
        //testSearchAll搜索全部
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        //设置源字段过滤 第一个参数是结果集包括哪些字段，第二个是结果集不包括哪些字段
        searchSourceBuilder.fetchSource(new String[]{"name","studymodel","price","timestamp"},new String[]{});
        //向搜索请求对象中设置搜索源
        searchRequest.source(searchSourceBuilder);
        //执行搜索，向ES发起HTTP请求
        SearchResponse search = client.search(searchRequest);
        //搜索结果
        SearchHits hits = search.getHits();
        //匹配到的总记录数
        long totalHits = hits.getTotalHits();
        //得到匹配度高的文档
        SearchHit[] hits1 = hits.getHits();
        //日期格式化对象
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (SearchHit hit : hits) {
            //文档的主键
            String id = hit.getId();
            //源文档内容
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            String studymodel = (String) sourceAsMap.get("studymodel");
            Double price = (Double) sourceAsMap.get("price");
            Date timestamp = simpleDateFormat.parse((String) sourceAsMap.get("timestamp"));
            System.out.println(name);
            System.out.println(price);
            System.out.println(timestamp);
            System.out.println(studymodel);
        }
        System.out.println(totalHits);
        System.out.println(hits1);
    }

    //分页查询
    @Test
    public void testSearchPage() throws IOException, ParseException {
        //搜索请求对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //指定类型
        searchRequest.types("doc");
        //搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //设置分页
        int page = 1;
        int size = 1;
        int from = (page - 1) * size;
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(size);
        //搜索方式
        //testSearchAll搜索全部
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        //设置源字段过滤 第一个参数是结果集包括哪些字段，第二个是结果集不包括哪些字段
        searchSourceBuilder.fetchSource(new String[]{"name","studymodel","price","timestamp"},new String[]{});
        //向搜索请求对象中设置搜索源
        searchRequest.source(searchSourceBuilder);
        //执行搜索，向ES发起HTTP请求
        SearchResponse search = client.search(searchRequest);
        //搜索结果
        SearchHits hits = search.getHits();
        //匹配到的总记录数
        long totalHits = hits.getTotalHits();
        //得到匹配度高的文档
        SearchHit[] hits1 = hits.getHits();
        //日期格式化对象
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (SearchHit hit : hits) {
            //文档的主键
            String id = hit.getId();
            //源文档内容
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            String studymodel = (String) sourceAsMap.get("studymodel");
            Double price = (Double) sourceAsMap.get("price");
//            Date timestamp = simpleDateFormat.parse((String) sourceAsMap.get("timestamp"));
            System.out.println(name);
            System.out.println(price);
//            System.out.println(timestamp);
            System.out.println(studymodel);
        }
        System.out.println(totalHits);
        System.out.println(hits1);
    }

    //精确查询
    @Test
    public void testSearchQuery() throws IOException, ParseException {
        //搜索请求对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //指定类型
        searchRequest.types("doc");
        //搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //搜索方式
        //termsQuery精确查询
        String[] ids = new String[]{"1","2"};
        searchSourceBuilder.query(QueryBuilders.termsQuery("_id",ids));
        //设置源字段过滤 第一个参数是结果集包括哪些字段，第二个是结果集不包括哪些字段
        searchSourceBuilder.fetchSource(new String[]{"name","studymodel","price","timestamp"},new String[]{});
        //向搜索请求对象中设置搜索源
        searchRequest.source(searchSourceBuilder);
        //执行搜索，向ES发起HTTP请求
        SearchResponse search = client.search(searchRequest);
        //搜索结果
        SearchHits hits = search.getHits();
        //匹配到的总记录数
        long totalHits = hits.getTotalHits();
        //得到匹配度高的文档
        SearchHit[] hits1 = hits.getHits();
        for (SearchHit hit : hits) {
            //文档的主键
            String id = hit.getId();
            //源文档内容
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            String studymodel = (String) sourceAsMap.get("studymodel");
            Double price = (Double) sourceAsMap.get("price");
            System.out.println(name);
            System.out.println(price);
            System.out.println(studymodel);
        }
        System.out.println(totalHits);
        System.out.println(hits1);
    }

    //multiMatchQuery
    @Test
    public void testSearchMultiQuery() throws IOException{
        //搜索请求对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //指定类型
        searchRequest.types("doc");
        //搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //搜索方式
        //设置"minimum_should_match": "70%"表示，三个词在文档的匹配占比为70%，即3*0.8=2.1，向上取整得2，表
        //示至少有两个词在文档中要匹配成功。
        searchSourceBuilder.query(QueryBuilders.multiMatchQuery("java css","name","description")
                                               .minimumShouldMatch("50%").field("name",10));
        //设置源字段过滤 第一个参数是结果集包括哪些字段，第二个是结果集不包括哪些字段
        searchSourceBuilder.fetchSource(new String[]{"name","studymodel","price","timestamp"},new String[]{});
        //向搜索请求对象中设置搜索源
        searchRequest.source(searchSourceBuilder);
        //执行搜索，向ES发起HTTP请求
        SearchResponse search = client.search(searchRequest);
        //搜索结果
        SearchHits hits = search.getHits();
        //匹配到的总记录数
        long totalHits = hits.getTotalHits();
        //得到匹配度高的文档
        SearchHit[] hits1 = hits.getHits();
        for (SearchHit hit : hits) {
            //文档的主键
            String id = hit.getId();
            //源文档内容
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            String studymodel = (String) sourceAsMap.get("studymodel");
            Double price = (Double) sourceAsMap.get("price");
            System.out.println(name);
            System.out.println(price);
            System.out.println(studymodel);
        }
        System.out.println(totalHits);
        System.out.println(hits1);
    }
    //MatchQuery
    @Test
    public void testSearchMatchQuery() throws IOException{
        //搜索请求对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //指定类型
        searchRequest.types("doc");
        //搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //搜索方式
        //设置"minimum_should_match": "70%"表示，三个词在文档的匹配占比为80%，即3*0.8=2.4，向上取整得2，表
        //示至少有两个词在文档中要匹配成功。
        searchSourceBuilder.query(QueryBuilders.matchQuery("description","java开发框架")
                .minimumShouldMatch("80%"));
        //设置源字段过滤 第一个参数是结果集包括哪些字段，第二个是结果集不包括哪些字段
        searchSourceBuilder.fetchSource(new String[]{"name","studymodel","price","timestamp"},new String[]{});
        //向搜索请求对象中设置搜索源
        searchRequest.source(searchSourceBuilder);
        //执行搜索，向ES发起HTTP请求
        SearchResponse search = client.search(searchRequest);
        //搜索结果
        SearchHits hits = search.getHits();
        //匹配到的总记录数
        long totalHits = hits.getTotalHits();
        //得到匹配度高的文档
        SearchHit[] hits1 = hits.getHits();
        for (SearchHit hit : hits) {
            //文档的主键
            String id = hit.getId();
            //源文档内容
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            String studymodel = (String) sourceAsMap.get("studymodel");
            Double price = (Double) sourceAsMap.get("price");
            System.out.println(name);
            System.out.println(price);
            System.out.println(studymodel);
        }
        System.out.println(totalHits);
        System.out.println(hits1);
    }

    //BoolQuery
    @Test
    public void testSearchBoolQuery() throws IOException{
        //搜索请求对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //指定类型
        searchRequest.types("doc");
        //搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //搜索方式BoolQuery
        //设置"minimum_should_match": "70%"表示，三个词在文档的匹配占比为80%，即3*0.8=2.4，向上取整得2，表
        //示至少有两个词在文档中要匹配成功。
        MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery("spring css", "name", "description")
                .minimumShouldMatch("50%").field("name", 10);
        //精确查询
        TermsQueryBuilder termsQueryBuilder = QueryBuilders.termsQuery("studymodel", "201001");
        //创建布尔查询对象
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        boolQueryBuilder.must(multiMatchQueryBuilder);
        boolQueryBuilder.must(termsQueryBuilder);
        //设置布尔查询
        searchSourceBuilder.query(boolQueryBuilder);
        //设置源字段过滤 第一个参数是结果集包括哪些字段，第二个是结果集不包括哪些字段
        searchSourceBuilder.fetchSource(new String[]{"name","studymodel","price","timestamp"},new String[]{});
        //向搜索请求对象中设置搜索源
        searchRequest.source(searchSourceBuilder);
        //执行搜索，向ES发起HTTP请求
        SearchResponse search = client.search(searchRequest);
        //搜索结果
        SearchHits hits = search.getHits();
        //匹配到的总记录数
        long totalHits = hits.getTotalHits();
        //得到匹配度高的文档
        SearchHit[] hits1 = hits.getHits();
        for (SearchHit hit : hits) {
            //文档的主键
            String id = hit.getId();
            //源文档内容
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            String studymodel = (String) sourceAsMap.get("studymodel");
            Double price = (Double) sourceAsMap.get("price");
            System.out.println(name);
            System.out.println(price);
            System.out.println(studymodel);
        }
        System.out.println(totalHits);
        System.out.println(hits1);
    }

    //filter过滤器
    @Test
    public void testSearchFilterQuery() throws IOException{
        //搜索请求对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //指定类型
        searchRequest.types("doc");
        //搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //搜索方式filter过滤 基于boolQuery
        //设置"minimum_should_match": "70%"表示，三个词在文档的匹配占比为80%，即3*0.8=2.4，向上取整得2，表
        //示至少有两个词在文档中要匹配成功。
        MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery("spring css", "name", "description")
                .minimumShouldMatch("50%").field("name", 10);
        //创建布尔查询对象
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        boolQueryBuilder.must(multiMatchQueryBuilder);
        //设置布尔查询
        searchSourceBuilder.query(boolQueryBuilder);
        boolQueryBuilder.filter(QueryBuilders.termQuery("studymodel","201001"));
        boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(60).lte(100));

        //设置源字段过滤 第一个参数是结果集包括哪些字段，第二个是结果集不包括哪些字段
        searchSourceBuilder.fetchSource(new String[]{"name","studymodel","price","timestamp"},new String[]{});
        //向搜索请求对象中设置搜索源
        searchRequest.source(searchSourceBuilder);
        //执行搜索，向ES发起HTTP请求
        SearchResponse search = client.search(searchRequest);
        //搜索结果
        SearchHits hits = search.getHits();
        //匹配到的总记录数
        long totalHits = hits.getTotalHits();
        //得到匹配度高的文档
        SearchHit[] hits1 = hits.getHits();
        for (SearchHit hit : hits) {
            //文档的主键
            String id = hit.getId();
            //源文档内容
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            String studymodel = (String) sourceAsMap.get("studymodel");
            Double price = (Double) sourceAsMap.get("price");
            System.out.println(name);
            System.out.println(price);
            System.out.println(studymodel);
        }
        System.out.println(totalHits);
        System.out.println(hits1);
    }

    //sort排序
    @Test
    public void testSearchSort() throws IOException{
        //搜索请求对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //指定类型
        searchRequest.types("doc");
        //搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //搜索方式 sort排序

        //创建布尔查询对象
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        //设置布尔查询
        searchSourceBuilder.query(boolQueryBuilder);
        boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(0).lte(100));

        //使用源对象
        searchSourceBuilder.sort(new FieldSortBuilder("studymodel").order(SortOrder.DESC));
        searchSourceBuilder.sort(new FieldSortBuilder("price").order(SortOrder.ASC));


        //设置源字段过滤 第一个参数是结果集包括哪些字段，第二个是结果集不包括哪些字段
        searchSourceBuilder.fetchSource(new String[]{"name","studymodel","price","timestamp"},new String[]{});
        //向搜索请求对象中设置搜索源
        searchRequest.source(searchSourceBuilder);
        //执行搜索，向ES发起HTTP请求
        SearchResponse search = client.search(searchRequest);
        //搜索结果
        SearchHits hits = search.getHits();
        //匹配到的总记录数
        long totalHits = hits.getTotalHits();
        //得到匹配度高的文档
        SearchHit[] hits1 = hits.getHits();
        for (SearchHit hit : hits) {
            //文档的主键
            String id = hit.getId();
            //源文档内容
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            String studymodel = (String) sourceAsMap.get("studymodel");
            Double price = (Double) sourceAsMap.get("price");
            System.out.println(name);
            System.out.println(price);
            System.out.println(studymodel);
        }
        System.out.println(totalHits);
        System.out.println(hits1);
    }

    //Highlight过滤器
    @Test
    public void testSearchHighlight() throws IOException{
        //搜索请求对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //指定类型
        searchRequest.types("doc");
        //搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //搜索方式filter过滤 基于boolQuery
        //设置"minimum_should_match": "70%"表示，三个词在文档的匹配占比为80%，即3*0.8=2.4，向上取整得2，表
        //示至少有两个词在文档中要匹配成功。
        MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery("开发框架", "name", "description")
                .minimumShouldMatch("50%").field("name", 10);
        //创建布尔查询对象
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        boolQueryBuilder.must(multiMatchQueryBuilder);
        //设置布尔查询
        searchSourceBuilder.query(boolQueryBuilder);
        boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(0).lte(100));

        //设置源字段过滤 第一个参数是结果集包括哪些字段，第二个是结果集不包括哪些字段
        searchSourceBuilder.fetchSource(new String[]{"name","studymodel","price","timestamp"},new String[]{});
        //设置高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.preTags("<Tag>");
        highlightBuilder.postTags("</Tag>");
        highlightBuilder.fields().add(new HighlightBuilder.Field("name"));

        searchSourceBuilder.highlighter(highlightBuilder);


        //向搜索请求对象中设置搜索源
        searchRequest.source(searchSourceBuilder);
        //执行搜索，向ES发起HTTP请求
        SearchResponse search = client.search(searchRequest);
        //搜索结果
        SearchHits hits = search.getHits();
        //匹配到的总记录数
        long totalHits = hits.getTotalHits();
        //得到匹配度高的文档
        SearchHit[] hits1 = hits.getHits();
        for (SearchHit hit : hits) {
            //文档的主键
            String id = hit.getId();
            //源文档内容
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            if (highlightBuilder != null){
                //取出高亮字段
                HighlightField highlightField = highlightFields.get("name");
                if (highlightField != null){
                    Text[] fragments = highlightField.getFragments();
                    StringBuffer stringBuffer = new StringBuffer();
                    for (Text fragment : fragments) {
                        stringBuffer.append(fragment);
                    }
                    name = stringBuffer.toString();
                }
            }

            String studymodel = (String) sourceAsMap.get("studymodel");
            Double price = (Double) sourceAsMap.get("price");
            System.out.println(name);
            System.out.println(price);
            System.out.println(studymodel);
        }
        System.out.println(totalHits);
        System.out.println(hits1);
    }
}
