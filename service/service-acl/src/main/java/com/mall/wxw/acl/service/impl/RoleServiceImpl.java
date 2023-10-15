package com.mall.wxw.acl.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.wxw.acl.mapper.RoleMapper;
import com.mall.wxw.acl.service.AdminRoleService;
import com.mall.wxw.acl.service.RoleService;
import com.mall.wxw.model.acl.AdminRole;
import com.mall.wxw.model.acl.Role;
import com.mall.wxw.vo.acl.RoleQueryVo;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author: wxw24633
 * @Time: 2023/10/14  23:31
 */
@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {
    @Resource
    private AdminRoleService adminRoleService;

    @Override
    public IPage<Role> selectRolePage(Page<Role> pageParam, RoleQueryVo roleQueryVo) {
        //获取条件值
        String roleName = roleQueryVo.getRoleName();
        //创建mp条件对象
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        //判断条件是否为空 不为空封装查询条件
        if (!StringUtils.isEmpty(roleName)){
            wrapper.like(Role::getRoleName,roleName);
        }
        //调用方法实现条件分页查询
        //返回分页对象
        return baseMapper.selectPage(pageParam, wrapper);
    }

    @Override
    public Map<String, Object> getRoleByAdminId(Long adminId) {
        //查询所有的角色
        List<Role> allRolesList =baseMapper.selectList(null);
        //拥有的角色id
        List<AdminRole> existUserRoleList = adminRoleService.list(new QueryWrapper<AdminRole>().eq("admin_id", adminId).select("role_id"));
        List<Long> existRoleList = existUserRoleList
                .stream()
                .map(AdminRole::getRoleId)
                .collect(Collectors.toList());
        //对角色进行分类
        List<Role> assignRoles = new ArrayList<Role>();
        for (Role role : allRolesList) {
            //已分配
            if(existRoleList.contains(role.getId())) {
                assignRoles.add(role);
            }
        }
        Map<String, Object> roleMap = new HashMap<>();
        roleMap.put("allRolesList", allRolesList);
        roleMap.put("assignRoles", assignRoles);
        return roleMap;
    }

    @Override
    public void saveAdminRole(Long adminId, Long[] roleIds) {
        //删除用户分配的角色数据
        adminRoleService.remove(new QueryWrapper<AdminRole>().eq("admin_id", adminId));
        //分配新的角色
        List<AdminRole> userRoleList = new ArrayList<>();
        for(Long roleId : roleIds) {
            if(StringUtils.isEmpty(roleId)) continue;
            AdminRole userRole = new AdminRole();
            userRole.setAdminId(adminId);
            userRole.setRoleId(roleId);
            userRoleList.add(userRole);
        }
        adminRoleService.saveBatch(userRoleList);
    }
}
