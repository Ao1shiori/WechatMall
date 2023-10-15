package com.mall.wxw.acl.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.wxw.acl.mapper.RolePermissionMapper;
import com.mall.wxw.acl.service.RolePermissionService;
import com.mall.wxw.model.acl.RolePermission;
import org.springframework.stereotype.Service;

/**
 * @author: wxw24633
 * @Time: 2023/10/15  14:54
 */
@Service
public class RolePermissionServiceImpl extends ServiceImpl<RolePermissionMapper, RolePermission> implements RolePermissionService {
}
