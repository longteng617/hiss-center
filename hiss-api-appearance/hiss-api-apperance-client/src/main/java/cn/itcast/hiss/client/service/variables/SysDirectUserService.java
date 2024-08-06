package cn.itcast.hiss.client.service.variables;

import cn.itcast.hiss.api.client.dto.VariablesGetDTO;
import cn.itcast.hiss.api.client.dto.VariablesQueryDTO;
import cn.itcast.hiss.api.client.dto.VariablesResultDTO;
import cn.itcast.hiss.api.client.dto.VariablesTreeDTO;

import java.util.List;

/**
 * SysDirectUserService
 *
 * @author: wgl
 * @describe: 系统指定用户service
 * @date: 2022/12/28 10:10
 */
public interface SysDirectUserService {

    /**
     * 根据id获取对象的方法
     * @param getDTO
     * @return
     */
    List<VariablesResultDTO> get(VariablesGetDTO getDTO);

    /**
     * 查询的方法
     * @param queryDTO
     * @return
     */
    List<VariablesResultDTO> query(VariablesQueryDTO queryDTO);

    /**
     * 树形结构查询
     * @param treeDTO
     * @return
     */
    List<VariablesResultDTO> tree(VariablesTreeDTO treeDTO);
}
