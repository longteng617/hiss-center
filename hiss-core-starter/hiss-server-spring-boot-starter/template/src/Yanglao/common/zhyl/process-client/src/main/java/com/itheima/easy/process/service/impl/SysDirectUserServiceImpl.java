package com.itheima.easy.process.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.itcast.hiss.api.client.dto.VariablesGetDTO;
import cn.itcast.hiss.api.client.dto.VariablesQueryDTO;
import cn.itcast.hiss.api.client.dto.VariablesResultDTO;
import cn.itcast.hiss.api.client.dto.VariablesTreeDTO;
import cn.itcast.hiss.client.service.variables.SysDirectUserService;
import com.itheima.easy.constant.SuperConstant;
import com.itheima.easy.entity.Dept;
import com.itheima.easy.entity.User;
import com.itheima.easy.mapper.DeptMapper;
import com.itheima.easy.mapper.HissMapper;
import com.itheima.easy.mapper.UserMapper;
import com.itheima.easy.service.DeptService;
import com.itheima.easy.utils.NoProcessing;
import com.itheima.easy.vo.DeptVo;
import com.itheima.easy.vo.UserVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/*
 * @author miukoo
 * @description 实现获取系统用户的接口
 * @date 2023/6/29 20:44
 * @version 1.0
 **/
@Service
public class SysDirectUserServiceImpl implements SysDirectUserService {

    @Autowired
    HissMapper hissMapper;

    @Autowired
    UserMapper userMapper;

    @Autowired
    DeptService deptService;

    @Autowired
    DeptMapper deptMapper;

    /**
     * 通过IDS获取用户：在通过id换取用户名时使用
     * @param getDTO
     * @return
     */
    @Override
    public List<VariablesResultDTO> get(VariablesGetDTO getDTO) {
        List<User> list = hissMapper.findUserListByIdsOrQuery(getDTO.getId(),null);
        return convertTo(list);
    }

    /**
     * 通过IDS、或者搜索关键字搜索用户：在配置流程时使用
     * @param queryDTO
     * @return
     */
    @Override
    public List<VariablesResultDTO> query(VariablesQueryDTO queryDTO) {
        List<User> list = hissMapper.findUserListByIdsOrQuery(null,queryDTO.getQuery());
        return convertTo(list);
    }

    /**
     * 在树形选择用户时使用，建议按以下规则实现：部门为可展开的节点，用户为可选择的节点
     * @param treeDTO
     * @return
     */
    @Override
    public List<VariablesResultDTO> tree(VariablesTreeDTO treeDTO) {
        String parentId = treeDTO.getParentId();
        if(StrUtil.isEmpty(parentId)){
            parentId = "100001000000000";
        }
        DeptVo deptVo = DeptVo.builder()
                .dataState(SuperConstant.DATA_STATE_0)
                .parentDeptNo(NoProcessing.processString(parentId))
                .build();
        // 获取部门信息
        List<Dept> deptList = deptMapper.selectList(deptVo);
        List<VariablesResultDTO> deptDTOList = new ArrayList<>();
        if(deptList!=null) {
            deptDTOList = deptList.stream().map(dept -> {
                VariablesResultDTO dto = new VariablesResultDTO();
                dto.setName(dept.getDeptName());
                dto.setId(dept.getDeptNo());
                dto.setSelect(false);
                return dto;
            }).collect(Collectors.toList());
        }
        // 获取部门人员信息
        List<VariablesResultDTO> userDTOList = new ArrayList<>();
        if(!"100001000000000".equalsIgnoreCase(parentId)) {
            UserVo userVo = new UserVo();
            userVo.setDeptNo(NoProcessing.processString(parentId));
            List<User> userList = userMapper.selectList(userVo);
            if (userList != null) {
                userDTOList = userList.stream().map(user -> {
                    VariablesResultDTO dto = new VariablesResultDTO();
                    dto.setName(user.getRealName());
                    dto.setId("" + user.getId());
                    dto.setSelect(true);
                    return dto;
                }).collect(Collectors.toList());
            }
        }
        // 合并结果
        deptDTOList.addAll(userDTOList);
        return deptDTOList;
    }

    private List<VariablesResultDTO> convertTo(List<User> list){
        if(list!=null){
            return list.stream().map( user -> {
                VariablesResultDTO dto = new VariablesResultDTO();
                dto.setName(user.getRealName());
                dto.setId(""+user.getId());
                dto.setSelect(true);
                return dto;
            }).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

}
