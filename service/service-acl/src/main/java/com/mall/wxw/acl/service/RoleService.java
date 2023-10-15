package com.mall.wxw.acl.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.wxw.model.acl.Role;
import com.mall.wxw.vo.acl.RoleQueryVo;

import java.util.Map;

/**
 * @author: wxw24633
 * @Time: 2023/10/14  23:29
 */
public interface RoleService extends IService<Role> {

    //角色条件分页查询
    IPage<Role> selectRolePage(Page<Role> pageParam, RoleQueryVo roleQueryVo);

    //查询用户已经分配角色
    Map<String, Object> getRoleByAdminId(Long adminId);

    //为用户分配角色
    void saveAdminRole(Long adminId, Long[] roleIds);
}
