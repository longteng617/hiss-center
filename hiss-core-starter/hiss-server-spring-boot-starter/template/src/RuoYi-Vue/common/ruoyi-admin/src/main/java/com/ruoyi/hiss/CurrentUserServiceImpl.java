package com.ruoyi.hiss;

import cn.itcast.hiss.message.CurrentUser;
import cn.itcast.hiss.sdk.web.oa.exception.HissException;
import cn.itcast.hiss.sdk.web.oa.service.CurrentUserService;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.utils.SecurityUtils;
import org.springframework.stereotype.Service;

/*
 * @author miukoo
 * @description 获取当前登录用户
 * @date 2023/7/12 10:53
 * @version 1.0
 **/
@Service
public class CurrentUserServiceImpl implements CurrentUserService {
    @Override
    public CurrentUser getCurrentUser() {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if(loginUser==null){
            throw new HissException("未找到当前登录的用户信息");
        }
        CurrentUser currentUser = new CurrentUser();
        currentUser.setUserId(""+loginUser.getUserId());
        currentUser.setUserName(loginUser.getUser().getUserName());
        currentUser.setDeptId(""+loginUser.getUser().getDeptId());
        return currentUser;
    }

    @Override
    public String getAdminId() {
        return "42376301c89b238420a7365bdf4f9797";
    }

}
