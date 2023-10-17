package com.mall.wxw.search.controller;

import com.mall.wxw.common.result.Result;
import com.mall.wxw.model.search.SkuEs;
import com.mall.wxw.search.service.SkuService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author: wxw24633
 * @Time: 2023/10/16  13:18
 */
@RestController
@RequestMapping("api/search/sku")
@Api(tags = "ES商品上下架接口")
public class SkuApiController {
    @Resource
    private SkuService skuService;

    @ApiOperation("商品上架")
    @GetMapping("inner/upperSku/{skuId}")
    public Result upperSku(@PathVariable Long skuId){
        skuService.upperSku(skuId);
        return Result.ok(null);
    }

    @ApiOperation("商品下架")
    @GetMapping("inner/lowerSku/{skuId}")
    public Result lowerSku(@PathVariable Long skuId){
        skuService.lowerSku(skuId);
        return Result.ok(null);
    }

    //获取爆款商品
    @ApiOperation(value = "获取爆品商品")
    @GetMapping("inner/findHotSkuList")
    public List<SkuEs> findHotSkuList() {
        return skuService.findHotSkuList();
    }

}
