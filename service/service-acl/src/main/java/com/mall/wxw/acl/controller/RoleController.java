package com.mall.wxw.acl.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.wxw.acl.service.RoleService;
import com.mall.wxw.common.result.Result;
import com.mall.wxw.model.acl.Role;
import com.mall.wxw.vo.acl.RoleQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author: wxw24633
 * @Time: 2023/10/14  23:27
 */
@Api(tags = "用户管理")
@RestController
@RequestMapping("/admin/acl/role")
@Slf4j
@CrossOrigin
public class RoleController {
    @Resource
    private RoleService roleService;

    @ApiOperation("角色条件分页")
    @GetMapping("/{current}/{limit}")
    public Result pageList(@PathVariable Long current,
                           @PathVariable Long limit,
                           RoleQueryVo roleQueryVo){
        //传递当前页和每页记录数
        Page<Role> pageParam = new Page<Role>(current,limit);
        //调用service返回分页对象
        IPage<Role> pageModel = roleService.selectRolePage(pageParam,roleQueryVo);
        return Result.ok(pageModel);
    }
    //根据id查角色

    //添加角色

    //修改角色

    //根据id删除角色

    //批量删除角色

}
