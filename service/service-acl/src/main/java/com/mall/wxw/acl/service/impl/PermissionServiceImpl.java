package com.mall.wxw.acl.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.wxw.acl.mapper.PermissionMapper;
import com.mall.wxw.acl.service.PermissionService;
import com.mall.wxw.acl.service.RolePermissionService;
import com.mall.wxw.acl.utils.PermissionHelper;
import com.mall.wxw.model.acl.Permission;
import com.mall.wxw.model.acl.RolePermission;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author: wxw24633
 * @Time: 2023/10/15  13:52
 */
@Service
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission> implements PermissionService {
    @Resource
    private RolePermissionService rolePermissionService;

    @Override
    public List<Permission> queryAllPermission() {
        //查询所有菜单
        List<Permission> permissionList = baseMapper.selectList(null);
        //转换要求格式
        List<Permission> result = PermissionHelper.buildPermission(permissionList);
        return result;
    }

    @Override
    public void removeChildById(Long id) {
        //idList有删除所有菜单id
        List<Long> idList = new ArrayList<>();
        //根据当前菜单获取子菜单id
        this.getAllPermissionId(id,idList);
        //设置当前菜单id
        idList.add(id);
        baseMapper.deleteBatchIds(idList);
    }

    @Override
    public List<Permission> findPermissionByRoleId(Long roleId) {
        //查所有权限
        List<Permission> permissionList = baseMapper.selectList(null);
        //拥有的权限id
        List<RolePermission> existRolePermissionList = rolePermissionService.list(new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getRoleId, roleId));
        List<Long> existPermissionList = existRolePermissionList
                .stream()
                .map(item -> item.getPermissionId())
                .collect(Collectors.toList());
        List<Permission> assignPermissions = new ArrayList<>();
        for (Permission permission : permissionList) {
            if (existPermissionList.contains(permission.getId())){
                assignPermissions.add(permission);
            }
        }
        return PermissionHelper.buildPermission(assignPermissions);
    }

    @Override
    public void saveRolePermissionRelationShip(Long roleId, Long[] permissionId) {
        //删除角色权限
        rolePermissionService.remove(new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getRoleId,roleId));
        //分配新权限
        List<RolePermission> rolePermissions = new ArrayList<>();
        for (Long aLong : permissionId) {
            if(StringUtils.isEmpty(aLong)) continue;
            RolePermission rolePermission = new RolePermission();
            rolePermission.setRoleId(roleId);
            rolePermission.setPermissionId(aLong);
            rolePermissions.add(rolePermission);
        }
        rolePermissionService.saveBatch(rolePermissions);
    }

    private void getAllPermissionId(Long id, List<Long> idList) {
        //根据id查子菜单
        LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Permission::getPid,id);
        List<Permission> childList = baseMapper.selectList(wrapper);
        childList.stream().forEach(item -> {
            idList.add(item.getId());
            //递归继续查
            this.getAllPermissionId(item.getId(),idList);
        });
    }
}
