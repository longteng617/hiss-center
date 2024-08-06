package com.itheima.easy.mapper;

import com.github.pagehelper.Page;
import com.itheima.easy.entity.User;
import com.itheima.easy.vo.UserVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface HissMapper {

    List<User> findUserListByIdsOrQuery(@Param("ids") List<String> ids,@Param("query") String query);

}
