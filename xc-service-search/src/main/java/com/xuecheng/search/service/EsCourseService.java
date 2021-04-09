package com.xuecheng.search.service;

import com.xuecheng.framework.domain.course.CoursePub;
import com.xuecheng.framework.domain.course.response.TeachplanMediaPub;
import com.xuecheng.framework.domain.search.CourseSearchParam;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.management.Query;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EsCourseService {

    @Value("${xuecheng.course.index}")
    private String index;
    @Value("${xuecheng.media.index}")
    private String media_index;

    @Value("${xuecheng.course.type}")
    private String doc;
    @Value("${xuecheng.media.type}")
    private String media_doc;

    //过滤的字段
    @Value("${xuecheng.course.source_field}")
    private String source_field;
    @Value("${xuecheng.media.source_field}")
    private String media_source_field;

    @Autowired
    RestHighLevelClient restHighLevelClient;

    public QueryResponseResult<CoursePub> list(int page, int size, CourseSearchParam courseSearchParam) {
        if (courseSearchParam == null){
            new CourseSearchParam();
        }
        //创建搜索请求对象
        SearchRequest searchRequest = new SearchRequest(index);
        //设置搜索的类型
        searchRequest.types(doc);
        //搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //过滤源字段
        String[] split = source_field.split(",");
        searchSourceBuilder.fetchSource(split,new String[]{});

        //创建布尔查询对象
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        //搜索条件 根据关键字搜索
        if (StringUtils.isNotEmpty(courseSearchParam.getKeyword())) {
            MultiMatchQueryBuilder matchQueryBuilder = QueryBuilders.multiMatchQuery(courseSearchParam.getKeyword(), "name", "description", "teachplan")
                    .minimumShouldMatch("70%")
                    .field("name", 10);
            boolQueryBuilder.must(matchQueryBuilder);
        }

        //精确查找根据分类、等级
        if (StringUtils.isNotEmpty(courseSearchParam.getMt())){
            //一级分类
            boolQueryBuilder.filter(QueryBuilders.termQuery("mt",courseSearchParam.getMt()));
        }
        if (StringUtils.isNotEmpty(courseSearchParam.getSt())){
            //二级分类
            boolQueryBuilder.filter(QueryBuilders.termQuery("st",courseSearchParam.getSt()));
        }
        if (StringUtils.isNotEmpty(courseSearchParam.getGrade())){
            //根据难度等级
            boolQueryBuilder.filter(QueryBuilders.termQuery("grade",courseSearchParam.getGrade()));
        }

        //分页
        if (page <= 0){
            page = 1;
        }
        if (size <= 0){
            size = 12;
        }
        int start = (page - 1) * size;
        searchSourceBuilder.from(start);
        searchSourceBuilder.size(size);

        //高亮显示
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.preTags("<font class='eslight'>");
        highlightBuilder.postTags("</font>");
        //设置高亮字段
        highlightBuilder.fields().add(new HighlightBuilder.Field("name"));
        searchSourceBuilder.highlighter(highlightBuilder);


        //将BoolQueryBuilder对象设置到searchSourceBuilder中
        searchSourceBuilder.query(boolQueryBuilder);
        //向搜索请求对象中设置搜索源
        searchRequest.source(searchSourceBuilder);

        //数据响应
        QueryResult<CoursePub> queryResult = new QueryResult<>();
        List<CoursePub> list = new ArrayList<>();
        try {
            //执行搜索，向ES发起HTTP请求
            SearchResponse search = restHighLevelClient.search(searchRequest);
            //结果集处理
            SearchHits hits = search.getHits();
            //获取总记录数
            long totalHits = hits.totalHits;
            queryResult.setTotal(totalHits);
            for (SearchHit hit : hits) {
                CoursePub coursePub = new CoursePub();
                //获取源文档
                Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                //在源文档取出name
                String name = (String) sourceAsMap.get("name");
                //取出ID
                String id = (String) sourceAsMap.get("id");
                coursePub.setId(id);
                //取出高亮字段name
                Map<String, HighlightField> highlightFields = hit.getHighlightFields();
                if (highlightFields != null){
                    HighlightField highlightFieldName = highlightFields.get("name");
                    if (highlightFieldName != null){
                        Text[] fragments = highlightFieldName.fragments();
                        //拼接高亮字段
                        StringBuffer stringBuffer = new StringBuffer();
                        for (Text fragment : fragments) {
                            stringBuffer.append(fragment);
                        }
                        name = stringBuffer.toString();
                    }
                }
                coursePub.setName(name);
                //图片
                String pic = (String) sourceAsMap.get("pic");
                coursePub.setPic(pic);
                //价格
                Double price = null;
                try {
                    if(sourceAsMap.get("price")!=null ){
                        price = (Double) sourceAsMap.get("price");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                coursePub.setPrice(price);
                Double price_old = null;
                try {
                    if(sourceAsMap.get("price_old")!=null ){
                        price_old = (Double) sourceAsMap.get("price_old");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                coursePub.setPrice_old(price_old);
                list.add(coursePub);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        queryResult.setList(list);
        QueryResponseResult<CoursePub> queryResponseResult = new QueryResponseResult<>(CommonCode.SUCCESS,queryResult);
        return queryResponseResult;
    }

    //使用ES客户端向ES请求搜索课程索引信息
    public Map<String,CoursePub> getall(String courseId) {
        //定义一个搜索请求对象
        SearchRequest searchRequest = new SearchRequest(index);
        searchRequest.types(doc);
        //定义SearchSourceBuilder设置查询字段
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //设置查询字段
        searchSourceBuilder.query(QueryBuilders.termsQuery("id",courseId));

        searchRequest.source(searchSourceBuilder);

        //最终要返回的课程信息
        CoursePub coursePub = new CoursePub();
        Map<String,CoursePub> map = new HashMap<>();
        try {
            //执行搜索，向ES发起HTTP请求
            SearchResponse search = restHighLevelClient.search(searchRequest);
            //获取内容
            SearchHits hits = search.getHits();
            SearchHit[] hitsHits = hits.getHits();
            for (SearchHit hitsHit : hitsHits) {
                //获取源文档内容
                Map<String, Object> sourceAsMap = hitsHit.getSourceAsMap();
                String id = (String) sourceAsMap.get("id");
                String name = (String) sourceAsMap.get("name");
                String grade = (String) sourceAsMap.get("grade");
                String charge = (String) sourceAsMap.get("charge");
                String pic = (String) sourceAsMap.get("pic");
                String description = (String) sourceAsMap.get("description");
                String teachplan = (String) sourceAsMap.get("teachplan");
                coursePub.setId(id);
                coursePub.setName(name);
                coursePub.setPic(pic);
                coursePub.setGrade(grade);
                coursePub.setTeachplan(teachplan);
                coursePub.setDescription(description);
                map.put(id,coursePub);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }

    //根据课程计划查询媒资信息
    public QueryResponseResult<TeachplanMediaPub> getmedia(String[] teachplanId) {
        //定义一个搜索请求对象
        SearchRequest searchRequest = new SearchRequest(media_index);
        //设置类型
        searchRequest.types(media_doc);

        //设置搜索源信息
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //设置要查询的字段 根据多个ID查询
        searchSourceBuilder.query(QueryBuilders.termsQuery("teachplan_id",teachplanId));
        //设置过滤字段，需要的字段
        String[] fetch = media_source_field.split(",");
        searchSourceBuilder.fetchSource(fetch,new String[]{});

        searchRequest.source(searchSourceBuilder);

        List<TeachplanMediaPub> mediaPubList = new ArrayList<>();
        long totalHits = 0;
        try {
            //使用es客户端进行查询 执行搜索
            SearchResponse search = restHighLevelClient.search(searchRequest);
            SearchHits hits = search.getHits();
            //匹配的条数
            totalHits = hits.totalHits;
            SearchHit[] hitsHits = hits.getHits();
            for (SearchHit hitsHit : hitsHits) {
                //将取出的信息存放在这里
                TeachplanMediaPub teachplanMediaPub = new TeachplanMediaPub();
                //匹配的源文档
                Map<String, Object> sourceAsMap = hitsHit.getSourceAsMap();
                //取出课程计划媒资信息
                String courseid = (String) sourceAsMap.get("courseid");
                String media_id = (String) sourceAsMap.get("media_id");
                String media_url = (String) sourceAsMap.get("media_url");
                String teachplan_id = (String) sourceAsMap.get("teachplan_id");
                String media_fileoriginalname = (String) sourceAsMap.get("media_fileoriginalname");

                teachplanMediaPub.setCourseId(courseid);
                teachplanMediaPub.setMediaUrl(media_url);
                teachplanMediaPub.setMediaFileOriginalName(media_fileoriginalname);
                teachplanMediaPub.setMediaId(media_id);
                teachplanMediaPub.setTeachplanId(teachplan_id);

                //将数据加入列表
                mediaPubList.add(teachplanMediaPub);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        QueryResult<TeachplanMediaPub> queryResult = new QueryResult();
        queryResult.setTotal(totalHits);
        queryResult.setList(mediaPubList);
        return new QueryResponseResult(CommonCode.SUCCESS,queryResult);
    }
}
