package com.mall.wxw.cart.controller;

import com.mall.wxw.cart.service.CartInfoService;
import com.mall.wxw.common.auth.AuthContextHolder;
import com.mall.wxw.common.result.Result;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author: wxw24633
 * @Time: 2023/10/17  16:17
 */
@RestController
@RequestMapping("/api/cart")
public class CarApiController {

    @Resource
    private CartInfoService cartInfoService;

    /**
     * 添加购物车
     *
     * @param skuId
     * @param skuNum
     * @return
     */
    @GetMapping("addToCart/{skuId}/{skuNum}")
    public Result addToCart(@PathVariable("skuId") Long skuId,
                            @PathVariable("skuNum") Integer skuNum) {
        // 如何获取userId
        Long userId = AuthContextHolder.getUserId();
        cartInfoService.addToCart(userId, skuId, skuNum);
        return Result.ok(null);
    }

    /**
     * 删除
     *
     * @param skuId
     * @param request
     * @return
     */
    @DeleteMapping("deleteCart/{skuId}")
    public Result deleteCart(@PathVariable("skuId") Long skuId,
                             HttpServletRequest request) {
        // 如何获取userId
        Long userId = AuthContextHolder.getUserId();
        cartInfoService.deleteCart(skuId, userId);
        return Result.ok(null);
    }

    @ApiOperation(value="清空购物车")
    @DeleteMapping("deleteAllCart")
    public Result deleteAllCart(HttpServletRequest request){
        // 如何获取userId
        Long userId = AuthContextHolder.getUserId();
        cartInfoService.deleteAllCart(userId);
        return Result.ok(null);
    }

    @ApiOperation(value="批量删除购物车")
    @PostMapping("batchDeleteCart")
    public Result batchDeleteCart(@RequestBody List<Long> skuIdList, HttpServletRequest request){
        // 如何获取userId
        Long userId = AuthContextHolder.getUserId();
        cartInfoService.batchDeleteCart(skuIdList, userId);
        return Result.ok(null);
    }
}
