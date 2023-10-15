package com.mall.wxw.sys.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.wxw.common.result.Result;
import com.mall.wxw.model.sys.Ware;
import com.mall.wxw.sys.service.WareService;
import com.mall.wxw.vo.product.WareQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * <p>
 * 仓库表 前端控制器
 * </p>
 *
 * @author wxw
 * @since 2023-10-15
 */
@RestController
@RequestMapping("/admin/sys/ware")
@CrossOrigin
@Api(tags = "仓库接口")
public class WareController {
    @Resource
    private WareService wareService;

    @ApiOperation("条件查询所有仓库")
    @GetMapping("/{page}/{limit}")
    public Result getPageList(@PathVariable Long page,
                              @PathVariable Long limit,
                              WareQueryVo wareQueryVo){
        Page<Ware> pageParam = new Page<>(page, limit);
        IPage<Ware> pageModel = wareService.selectWarePage(pageParam,wareQueryVo);
        return Result.ok(pageModel);

    }

    @ApiOperation("查询所有仓库")
    @GetMapping("findAllList")
    public Result findAllList(){
        return Result.ok(wareService.list());
    }

}

