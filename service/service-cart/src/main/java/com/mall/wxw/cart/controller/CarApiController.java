package com.mall.wxw.cart.controller;

import com.mall.wxw.cart.service.CartInfoService;
import com.mall.wxw.client.activity.ActivityFeignClient;
import com.mall.wxw.common.auth.AuthContextHolder;
import com.mall.wxw.common.result.Result;
import com.mall.wxw.model.order.CartInfo;
import com.mall.wxw.vo.order.OrderConfirmVo;
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

    @Resource
    private ActivityFeignClient activityFeignClient;

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
    public Result deleteCart(@PathVariable("skuId") Long skuId, HttpServletRequest request) {
        // 获取用户 ID
        Long userId = AuthContextHolder.getUserId();
        // 调用购物车服务删除购物车中指定 SKU 的商品
        cartInfoService.deleteCart(skuId, userId);
        // 返回删除成功的响应结果
        return Result.ok(null);
    }


    @ApiOperation(value="清空购物车")
    @DeleteMapping("deleteAllCart")
    public Result deleteAllCart(HttpServletRequest request){
        Long userId = AuthContextHolder.getUserId();
        cartInfoService.deleteAllCart(userId);
        return Result.ok(null);
    }

    @ApiOperation(value="批量删除购物车")
    @PostMapping("batchDeleteCart")
    public Result batchDeleteCart(@RequestBody List<Long> skuIdList, HttpServletRequest request) {
        // 获取用户 ID
        Long userId = AuthContextHolder.getUserId();
        // 调用购物车服务批量删除购物车中指定 SKU 的商品
        cartInfoService.batchDeleteCart(skuIdList, userId);
        // 返回删除成功的响应结果
        return Result.ok(null);
    }


    /**
     * 查询购物车列表
     *
     * @param request
     * @return
     */
    @GetMapping("cartList")
    public Result cartList(HttpServletRequest request) {
        // 获取用户 ID
        Long userId = AuthContextHolder.getUserId();
        // 根据用户 ID 获取购物车列表
        List<CartInfo> cartInfoList = cartInfoService.getCartList(userId);
        // 返回包含购物车列表的响应结果
        return Result.ok(cartInfoList);
    }


    /**
     * 查询带优惠卷的购物车
     *
     * @param request
     * @return
     */
    @GetMapping("activityCartList")
    public Result activityCartList(HttpServletRequest request) {
        // 获取用户 ID
        Long userId = AuthContextHolder.getUserId();

        // 从购物车服务获取用户的购物车列表
        List<CartInfo> cartInfoList = cartInfoService.getCartList(userId);

        // 使用活动 Feign 客户端查询购物车活动信息和优惠券
        OrderConfirmVo orderTradeVo = activityFeignClient.findCartActivityAndCoupon(cartInfoList, userId);

        // 返回包含购物车活动信息和优惠券的响应结果
        return Result.ok(orderTradeVo);
    }


    /**
     * 更新选中状态
     *
     * @param skuId
     * @param isChecked
     * @param request
     * @return
     */
    @GetMapping("checkCart/{skuId}/{isChecked}")
    public Result checkCart(@PathVariable(value = "skuId") Long skuId,
                            @PathVariable(value = "isChecked") Integer isChecked, HttpServletRequest request) {
        // 获取用户Id
        Long userId = AuthContextHolder.getUserId();
        // 调用更新方法
        cartInfoService.checkCart(userId, isChecked, skuId);
        return Result.ok(null);
    }

    @GetMapping("checkAllCart/{isChecked}")
    public Result checkAllCart(@PathVariable(value = "isChecked") Integer isChecked, HttpServletRequest request) {
        // 获取用户Id
        Long userId = AuthContextHolder.getUserId();
        // 调用更新方法
        cartInfoService.checkAllCart(userId, isChecked);
        return Result.ok(null);
    }

    @ApiOperation(value="批量选择购物车")
    @PostMapping("batchCheckCart/{isChecked}")
    public Result batchCheckCart(@RequestBody List<Long> skuIdList, @PathVariable(value = "isChecked") Integer isChecked, HttpServletRequest request){
        // 如何获取userId
        Long userId = AuthContextHolder.getUserId();
        cartInfoService.batchCheckCart(skuIdList, userId, isChecked);
        return Result.ok(null);
    }

    //获取购物车选中购物项
    /**
     * 根据用户Id 查询购物车列表
     *
     * @param userId
     * @return
     */
    @GetMapping("inner/getCartCheckedList/{userId}")
    public List<CartInfo> getCartCheckedList(@PathVariable("userId") Long userId) {
        return cartInfoService.getCartCheckedList(userId);
    }
}
