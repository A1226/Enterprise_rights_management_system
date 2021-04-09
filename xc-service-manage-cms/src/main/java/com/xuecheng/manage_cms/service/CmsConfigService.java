package com.xuecheng.manage_cms.service;

import com.xuecheng.framework.domain.cms.CmsConfig;
import com.xuecheng.manage_cms.dao.CmsConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CmsConfigService {

    @Autowired
    private CmsConfigRepository cmsConfigRepository;

    public CmsConfig getConfigById(String id){
        Optional<CmsConfig> configId = cmsConfigRepository.findById(id);
        if (configId.isPresent()){
            CmsConfig cmsConfig = configId.get();
            return cmsConfig;
        }
        return null;
    }
}
