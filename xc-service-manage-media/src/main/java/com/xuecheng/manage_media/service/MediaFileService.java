package com.xuecheng.manage_media.service;


import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.request.QueryMediaFileRequest;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.manage_media.dao.MediaFileRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Administrator
 * @version 1.0
 **/
@Service
public class MediaFileService {

    //持久层dao
    @Autowired
    MediaFileRepository mediaFileRepository;

    public QueryResponseResult<MediaFile> findList(int page, int size, QueryMediaFileRequest queryMediaFileRequest){
        if (queryMediaFileRequest == null){
            new QueryMediaFileRequest();
        }

        //设置分页参数
        if (page <= 0){
            page = 1;
        }
        page = page - 1;
        if (size <= 0){
            size = 10;
        }
        //条件值对象
        MediaFile mediaFile = new MediaFile();
        if (StringUtils.isNotEmpty(queryMediaFileRequest.getTag())){
            mediaFile.setTag(queryMediaFileRequest.getTag());
        }
        if (StringUtils.isNotEmpty(queryMediaFileRequest.getFileOriginalName())){
            mediaFile.setFileOriginalName(queryMediaFileRequest.getFileOriginalName());
        }
        if (StringUtils.isNotEmpty(queryMediaFileRequest.getProcessStatus())){
            mediaFile.setProcessStatus(queryMediaFileRequest.getProcessStatus());
        }

        //条件匹配器
        ExampleMatcher exampleMatcher = ExampleMatcher.matching()
                .withMatcher("tag",ExampleMatcher.GenericPropertyMatchers.contains())
                .withMatcher("fileOriginalName",ExampleMatcher.GenericPropertyMatchers.contains());

        //定义example条件对象
        Example<MediaFile> example = Example.of(mediaFile,exampleMatcher);

        //设置查询分页参数
        Pageable pageable = new PageRequest(page,size);
        //分页查询
        Page<MediaFile> filePage = mediaFileRepository.findAll(example, pageable);
        //获取总记录数
        long totalElements = filePage.getTotalElements();
        //获取数据列表
        List<MediaFile> content = filePage.getContent();

        //返回数据集
        QueryResult<MediaFile> queryResult = new QueryResult<>();
        queryResult.setList(content);
        queryResult.setTotal(totalElements);

        return new QueryResponseResult(CommonCode.SUCCESS,queryResult);
    }


}
