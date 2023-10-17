package com.mall.wxw.home.controller;

import com.mall.wxw.common.auth.AuthContextHolder;
import com.mall.wxw.common.result.Result;
import com.mall.wxw.home.service.ItemService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author: wxw24633
 * @Time: 2023/10/17  14:53
 */


@Api(tags = "商品详情")
@RestController
@RequestMapping("api/home")
public class ItemApiController {
    @Resource
    private ItemService itemService;

    @ApiOperation(value = "获取sku详细信息")
    @GetMapping("item/{id}")
    public Result index(@PathVariable Long id, HttpServletRequest request) {
        Long userId = AuthContextHolder.getUserId();
        Map<String,Object> data = itemService.item(id, userId);
        return Result.ok(data);
    }
}
