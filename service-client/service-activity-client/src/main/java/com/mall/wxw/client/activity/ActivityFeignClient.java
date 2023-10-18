package com.mall.wxw.client.activity;

import com.mall.wxw.model.activity.CouponInfo;
import com.mall.wxw.model.order.CartInfo;
import com.mall.wxw.vo.order.CartInfoVo;
import com.mall.wxw.vo.order.OrderConfirmVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@FeignClient("service-activity")
public interface ActivityFeignClient {

    //根据skuId列表获取促销信息
    @PostMapping("/api/activity/inner/findActivity")
    Map<Long, List<String>> findActivity(@RequestBody List<Long> skuIdList);

    @ApiOperation(value = "根据skuId获取促销与优惠券信息")
    @GetMapping("/api/activity/inner/findActivityAndCoupon/{skuId}/{userId}")
    Map<String, Object> findActivityAndCoupon(@PathVariable Long skuId, @PathVariable("userId") Long userId);

    @ApiOperation(value = "获取购物车满足条件的促销与优惠券信息")
    @PostMapping("/api/activity/inner/findCartActivityAndCoupon/{userId}")
    OrderConfirmVo findCartActivityAndCoupon(@RequestBody List<CartInfo> cartInfoList, @PathVariable("userId") Long userId);


    @ApiOperation(value = "购物车对应规则数据")
    @PostMapping("/api/activity/inner/findCartActivityList")
    List<CartInfoVo> findCartActivityList(@RequestBody List<CartInfo> cartInfoList);

    @ApiOperation(value = "购物车对应优惠券")
    @PostMapping("/api/activity/inner/findRangeSkuIdList/{couponId}")
    CouponInfo findRangeSkuIdList(@RequestBody List<CartInfo> cartInfoList, @PathVariable("couponId") Long couponId);

    @ApiOperation(value = "更新优惠券使用状态")
    @PostMapping("/api/activity/inner/updateCouponInfoUseStatus/{couponId}/{userId}/{orderId}")
    Boolean updateCouponInfoUseStatus(@PathVariable Long couponId,
                                             @PathVariable Long userId,
                                             @PathVariable Long orderId);
}