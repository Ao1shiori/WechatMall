package com.mall.wxw.home.controller;

import com.mall.wxw.client.product.ProductFeignClient;
import com.mall.wxw.common.result.Result;
import com.mall.wxw.vo.search.SkuEsQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author: wxw24633
 * @Time: 2023/10/17  13:04
 */

@Api(tags = "商品分类")
@RestController
@RequestMapping("api/home")
public class CategoryApiController {

    @Resource
    private ProductFeignClient productFeignClient;

    @ApiOperation(value = "获取分类信息")
    @GetMapping("category")
    public Result categoryList() {
        return Result.ok(productFeignClient.findAllCategoryList());
    }


}
