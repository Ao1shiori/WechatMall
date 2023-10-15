package com.mall.wxw.sys.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.wxw.common.result.Result;
import com.mall.wxw.model.sys.RegionWare;
import com.mall.wxw.sys.service.RegionWareService;
import com.mall.wxw.vo.sys.RegionWareQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 城市仓库关联表 前端控制器
 * </p>
 *
 * @author wxw
 * @since 2023-10-15
 */
@Api(tags = "区域仓库接口")
@CrossOrigin
@RestController
@RequestMapping("/admin/sys/regionWare")
public class RegionWareController {
    @Resource
    private RegionWareService regionWareService;

    @ApiOperation("开通区域列表")
    @GetMapping("/{page}/{limit}")
    public Result getPageList(@PathVariable Long page,
                              @PathVariable Long limit,
                              RegionWareQueryVo regionWareQueryVo){
        Page<RegionWare> pageParam = new Page<>(page, limit);
        IPage<RegionWare> pageModel = regionWareService.selectRegionWarePage(pageParam,regionWareQueryVo);
        return Result.ok(pageModel);
    }



    @ApiOperation("根据id查询区域")
    @GetMapping("/get/{id}")
    public Result getById(@PathVariable Long id){
        return Result.ok(regionWareService.getById(id));
    }

    @ApiOperation("添加开通区域")
    @PostMapping("/save")
    public Result save(@RequestBody RegionWare regionWare){
        regionWareService.saveRegionWare(regionWare);
        if (regionWareService.save(regionWare)){
            return Result.ok(null);
        }
        return Result.fail(null);
    }

    @ApiOperation("根据id删除区域")
    @DeleteMapping("/remove/{id}")
    public Result remove(@PathVariable Long id){
        if (regionWareService.removeById(id)){
            return Result.ok(null);
        }
        return Result.fail(null);
    }

    @ApiOperation("批量删除区域")
    @DeleteMapping("/batchRemove")
    public Result removeRows(@RequestBody List<Long> idList){
        if (regionWareService.removeByIds(idList)){
            return Result.ok(null);
        }
        return Result.fail(null);
    }

}

