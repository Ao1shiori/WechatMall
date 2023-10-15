package com.mall.wxw.acl.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.wxw.acl.mapper.AdminMapper;
import com.mall.wxw.acl.service.AdminService;
import com.mall.wxw.model.acl.Admin;
import com.mall.wxw.vo.acl.AdminQueryVo;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * @author: wxw24633
 * @Time: 2023/10/15  11:42
 */
@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {
    @Override
    public IPage<Admin> selectAdminPage(Page<Admin> pageParam, AdminQueryVo adminQueryVo) {
        String name = adminQueryVo.getName();
        String username = adminQueryVo.getUsername();
        LambdaQueryWrapper<Admin> wrapper = new LambdaQueryWrapper<>();
        if (!StringUtils.isEmpty(name)){
            wrapper.like(Admin::getName,name);
        }
        if (!StringUtils.isEmpty(username)){
            wrapper.like(Admin::getUsername,username);
        }
        return baseMapper.selectPage(pageParam, wrapper);
    }
}
