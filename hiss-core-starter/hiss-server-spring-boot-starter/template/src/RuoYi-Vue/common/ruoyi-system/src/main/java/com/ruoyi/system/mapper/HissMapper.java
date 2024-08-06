package com.ruoyi.system.mapper;

import com.ruoyi.common.core.domain.entity.SysUser;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/*
 * @author miukoo
 * @description //TODO
 * @date 2023/7/13 17:34
 * @version 1.0
 **/
public interface HissMapper {
    List<SysUser> findUserListByIdsOrQuery(@Param("ids") List<String> ids, @Param("query") String query);

    List<SysUser> findUserListByDeptId(@Param("deptId") String deptId);
}
