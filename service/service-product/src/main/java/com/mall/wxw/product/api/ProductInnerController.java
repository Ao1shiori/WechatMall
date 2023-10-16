package com.mall.wxw.product.api;

import com.mall.wxw.model.product.Category;
import com.mall.wxw.model.product.SkuInfo;
import com.mall.wxw.product.service.CategoryService;
import com.mall.wxw.product.service.SkuInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author: wxw24633
 * @Time: 2023/10/16  13:25
 */

@RestController
@RequestMapping("/api/product")
@Api(tags = "商品远程调用接口")
public class ProductInnerController {
    @Resource
    private CategoryService categoryService;

    @Resource
    private SkuInfoService skuInfoService;

    @ApiOperation(value = "根据分类id获取分类信息")
    @GetMapping("inner/getCategory/{categoryId}")
    public Category getCategory(@PathVariable Long categoryId) {
        return categoryService.getById(categoryId);
    }

    @ApiOperation(value = "根据skuId获取sku信息")
    @GetMapping("inner/getSkuInfo/{skuId}")
    public SkuInfo getSkuInfo(@PathVariable("skuId") Long skuId) {
        return skuInfoService.getById(skuId);
    }
}
