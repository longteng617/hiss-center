package com.itheima.easy.process.service.impl;

import cn.itcast.hiss.message.CurrentUser;
import cn.itcast.hiss.sdk.web.oa.exception.HissException;
import cn.itcast.hiss.sdk.web.oa.service.CurrentUserService;
import com.alibaba.fastjson.JSONObject;
import com.itheima.easy.utils.UserThreadLocal;
import com.itheima.easy.vo.UserVo;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

/*
 * @author miukoo
 * @description //TODO
 * @date 2023/7/12 10:53
 * @version 1.0
 **/
@Service
public class CurrentUserServiceImpl implements CurrentUserService {
    @Override
    public CurrentUser getCurrentUser() {
        String subject = UserThreadLocal.getSubject();
        UserVo userVo = JSONObject.parseObject(subject,UserVo.class);
        if(userVo==null){
            throw new HissException("未找到当前登录的用户信息");
        }
        CurrentUser currentUser = new CurrentUser();
        currentUser.setUserId(""+userVo.getId());
        currentUser.setUserName(userVo.getRealName());
        currentUser.setDeptId(userVo.getDeptNo());
        currentUser.setDeptName(userVo.getDept());
        currentUser.setRoleId(userVo.getRoleVoIds()!=null?userVo.getRoleVoIds().stream().collect(Collectors.joining(",")):"");
        currentUser.setRoleName(userVo.getRoleLabels()!=null?userVo.getRoleLabels().stream().collect(Collectors.joining(",")):"");
        currentUser.setPostId(userVo.getPostNo());
        currentUser.setPostName(userVo.getPost());
        return currentUser;
    }
}
