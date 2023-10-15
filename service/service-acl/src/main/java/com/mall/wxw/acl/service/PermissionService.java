package com.mall.wxw.acl.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.wxw.model.acl.Permission;

import java.util.List;

/**
 * @author: wxw24633
 * @Time: 2023/10/15  13:52
 */
public interface PermissionService extends IService<Permission> {

    //获取权限(菜单/功能)列表
    List<Permission> queryAllPermission();

    //递归删除菜单
    void removeChildById(Long id);

    List<Permission> findPermissionByRoleId(Long roleId);

    void saveRolePermissionRelationShip(Long roleId, Long[] permissionId);
}
