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
import java.util.List;

/**
 * @author: wxw24633
 * @Time: 2023/10/14  23:27
 */
@Api(tags = "角色接口")
@RestController
@RequestMapping("/admin/acl/role")
@Slf4j
public class RoleController {
    @Resource
    private RoleService roleService;

    @ApiOperation("角色条件分页")
    @GetMapping("/{current}/{limit}")
    public Result pageList(@PathVariable Long current,
                           @PathVariable Long limit,
                           RoleQueryVo roleQueryVo){
        //传递当前页和每页记录数
        Page<Role> pageParam = new Page<>(current, limit);
        //调用service返回分页对象
        IPage<Role> pageModel = roleService.selectRolePage(pageParam,roleQueryVo);
        return Result.ok(pageModel);
    }

    @ApiOperation("根据id查角色")
    @GetMapping("/get/{id}")
    public Result get(@PathVariable Long id){
        return Result.ok(roleService.getById(id));
    }

    @ApiOperation("添加角色")
    @PostMapping("/save")
    public Result save(@RequestBody Role role){
        if (roleService.save(role)){
            return Result.ok(null);
        }
        return Result.fail(null);
    }

    @ApiOperation("修改角色")
    @PutMapping("/update")
    public Result update(@RequestBody Role role){
        if (roleService.updateById(role)){
            return Result.ok(null);
        }
        return Result.fail(null);
    }

    @ApiOperation("根据id删除角色")
    @DeleteMapping("/remove/{id}")
    public Result remove(@PathVariable Long id){
        if (roleService.removeById(id)){
            return Result.ok(null);
        }
        return Result.fail(null);
    }

    //json数组[1,2,3]对应java集合
    @ApiOperation("批量删除角色")
    @DeleteMapping("/batchRemove")
    public Result batchRemove(@RequestBody List<Long> idList){
        if (roleService.removeByIds(idList)){
            return Result.ok(null);
        }
        return Result.fail(null);
    }

}
