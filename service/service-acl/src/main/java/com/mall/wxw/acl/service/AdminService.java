package com.mall.wxw.acl.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.wxw.model.acl.Admin;
import com.mall.wxw.vo.acl.AdminQueryVo;

/**
 * @author: wxw24633
 * @Time: 2023/10/15  11:41
 */
public interface AdminService extends IService<Admin> {
    IPage<Admin> selectAdminPage(Page<Admin> pageParam, AdminQueryVo adminQueryVo);
}
