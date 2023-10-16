package com.mall.wxw.sys.controller;


import com.mall.wxw.common.result.Result;
import com.mall.wxw.model.sys.Region;
import com.mall.wxw.sys.service.RegionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 地区表 前端控制器
 * </p>
 *
 * @author wxw
 * @since 2023-10-15
 */
@Api(tags = "区域接口")
@RestController
@RequestMapping("/admin/sys/region")
public class RegionController {
    @Resource
    private RegionService regionService;

    @ApiOperation("根据关键字查区域信息")
    @GetMapping("/findRegionByKeyword/{keyword}")
    public Result findRegionByKeyword(@PathVariable("keyword") String keyword){
        List<Region> list =regionService.getRegionByKeyword(keyword);
        return Result.ok(list);
    }


}

