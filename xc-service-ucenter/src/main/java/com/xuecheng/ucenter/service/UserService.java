package com.xuecheng.ucenter.service;

import com.xuecheng.framework.domain.ucenter.XcCompanyUser;
import com.xuecheng.framework.domain.ucenter.XcMenu;
import com.xuecheng.framework.domain.ucenter.XcUser;
import com.xuecheng.framework.domain.ucenter.ext.XcUserExt;
import com.xuecheng.ucenter.dao.XcCompanyUserRepository;
import com.xuecheng.ucenter.dao.XcMenuMapper;
import com.xuecheng.ucenter.dao.XcUserRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    XcUserRepository xcUserRepository;

    @Autowired
    XcCompanyUserRepository xcCompanyUserRepository;

    @Autowired
    XcMenuMapper xcMenuMapper;

    //根据用户账号查询用户信息
    public XcUser findXcUserByUsername(String userName){
        //根据账号查询用户的信息
        return xcUserRepository.findByUsername(userName);
    }

    //根据账号查询用户的信息，返回用户扩展信息
    public XcUserExt getUserExt(String userName){
        //根据账号查询用户的信息
        XcUser xcUser = this.findXcUserByUsername(userName);
        if (xcUser == null){
            return null;
        }
        //获取信息
        XcUserExt xcUserExt = new XcUserExt();
        BeanUtils.copyProperties(xcUser,xcUserExt);

        //用户ID
        String id = xcUserExt.getId();

        //查询用户所有权限
        List<XcMenu> xcMenus = xcMenuMapper.selectPermissionByUserId(id);
        xcUserExt.setPermissions(xcMenus);

        //根据用户ID查询用户所属公司ID
        XcCompanyUser byUserId = xcCompanyUserRepository.findByUserId(id);
        if (byUserId != null){
            String companyId = byUserId.getCompanyId();
            xcUserExt.setCompanyId(companyId);
        }
        return xcUserExt;
    }

}
