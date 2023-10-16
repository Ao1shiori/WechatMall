package com.mall.wxw.acl.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.wxw.acl.service.AdminService;
import com.mall.wxw.acl.service.RoleService;
import com.mall.wxw.acl.service.AdminRoleService;
import com.mall.wxw.common.result.Result;
import com.mall.wxw.common.utils.MD5;
import com.mall.wxw.model.acl.Admin;
import com.mall.wxw.vo.acl.AdminQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author: wxw24633
 * @Time: 2023/10/15  11:39
 */
@RestController
@Api(tags = "用户接口")
@RequestMapping("/admin/acl/user")
public class AdminController {

    @Resource
    private AdminService adminService;

    @Resource
    private RoleService roleService;

    @Resource
    private AdminRoleService adminRoleService;

    @ApiOperation("用户列表条件分页")
    @GetMapping("/{current}/{limit}")
    public Result getPageList(@PathVariable Long current,
                              @PathVariable Long limit,
                              AdminQueryVo adminQueryVo){
        Page<Admin> pageParam = new Page<>(current, limit);
        IPage<Admin> pageModel = adminService.selectAdminPage(pageParam,adminQueryVo);
        return Result.ok(pageModel);
    }

    @ApiOperation("id查用户")
    @GetMapping("/get/{id}")
    public Result getById(@PathVariable Long id){
        Admin admin = adminService.getById(id);
        return Result.ok(admin);
    }

    @ApiOperation("添加用户")
    @PostMapping("/save")
    public Result add(@RequestBody Admin admin){
        String password = admin.getPassword();
        String passwordMD5 = MD5.encrypt(password);
        admin.setPassword(passwordMD5);
        if (adminService.save(admin)){
            return Result.ok(null);
        }
        return Result.fail(null);
    }

    @ApiOperation("修改用户")
    @PutMapping("/update")
    public Result update(@RequestBody Admin admin){
        if (adminService.updateById(admin)){
            return Result.ok(null);
        }
        return Result.fail(null);
    }

    @ApiOperation("根据id删除用户")
    @DeleteMapping("/remove/{id}")
    public Result removeById(@PathVariable Long id){
        if (adminService.removeById(id)){
            return Result.ok(null);
        }
        return Result.fail(null);
    }

    @ApiOperation("批量删除用户")
    @DeleteMapping("/batchRemove")
    public Result removeUsers(@RequestBody List<Long> idList){
        if (adminService.removeByIds(idList)){
            return Result.ok(null);
        }
        return Result.fail(null);
    }

    @ApiOperation("获取用户角色")
    @GetMapping("/toAssign/{adminId}")
    public Result getRoles(@PathVariable Long adminId){
        Map<String,Object> map = roleService.getRoleByAdminId(adminId);
        return Result.ok(map);
    }

    @ApiOperation("为用户分配角色")
    @PostMapping("/doAssign")
    public Result assignRoles(@RequestParam Long adminId,
                              @RequestParam Long[] roleId){
        roleService.saveAdminRole(adminId,roleId);
        return Result.ok(null);

    }
}
