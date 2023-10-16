package com.mall.wxw.acl.controller;

import com.mall.wxw.acl.service.PermissionService;
import com.mall.wxw.common.result.Result;
import com.mall.wxw.model.acl.Permission;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author: wxw24633
 * @Time: 2023/10/15  13:50
 */
@RestController
@Api(tags = "菜单管理")
@RequestMapping("/admin/acl/permission")
public class PermissionController {

    @Resource
    private PermissionService permissionService;

    @ApiOperation("获取权限(菜单/功能)列表")
    @GetMapping
    public Result getPermissionList(){
        List<Permission> list = permissionService.queryAllPermission();
        return Result.ok(list);
    }

    @ApiOperation("删除一个权限项")
    @DeleteMapping("/remove/{id}")
    public Result removePermission(@PathVariable Long id){
        permissionService.removeChildById(id);
        return Result.ok(null);
    }

    @ApiOperation("保存一个权限项")
    @PostMapping("/save")
    public Result addPermission(@RequestBody Permission permission){
        if (permissionService.save(permission)){
            return Result.ok(null);
        }
        return Result.fail(null);
    }


    @ApiOperation("更新一个权限项")
    @PutMapping("/update")
    public Result updatePermission(@RequestBody Permission permission){
        if (permissionService.updateById(permission)){
            return Result.ok(null);
        }
        return Result.fail(null);
    }


    @ApiOperation("查看某个角色的权限列表")
    @GetMapping("/toAssign/{roleId}")
    public Result toAssign(@PathVariable Long roleId){
        List<Permission> permissionByRoleId = permissionService.findPermissionByRoleId(roleId);
        return Result.ok(permissionByRoleId);
    }



    @ApiOperation("给某个角色授权")
    @PostMapping("/doAssign")
    public Result doAssign(@RequestParam Long roleId,
                           @RequestParam Long[] permissionId){
        permissionService.saveRolePermissionRelationShip(roleId,permissionId);
        return Result.ok(null);

    }


}
