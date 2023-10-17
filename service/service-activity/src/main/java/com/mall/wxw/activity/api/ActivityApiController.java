package com.mall.wxw.activity.api;

import com.mall.wxw.activity.service.ActivityInfoService;
import com.mall.wxw.model.order.CartInfo;
import com.mall.wxw.vo.order.OrderConfirmVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Api(tags = "促销与优惠券接口")
@RestController
@RequestMapping("/api/activity")
@Slf4j
public class ActivityApiController {

	@Resource
	private ActivityInfoService activityInfoService;

	@ApiOperation(value = "根据skuId列表获取促销信息")
	@PostMapping("inner/findActivity")
	public Map<Long, List<String>> findActivity(@RequestBody List<Long> skuIdList) {
		return activityInfoService.findActivity(skuIdList);
	}

	@ApiOperation(value = "根据skuId获取促销与优惠券信息")
	@GetMapping("inner/findActivityAndCoupon/{skuId}/{userId}")
	public Map<String, Object> findActivityAndCoupon(@PathVariable Long skuId, @PathVariable("userId") Long userId) {
		return activityInfoService.findActivityAndCoupon(skuId, userId);
	}

	@ApiOperation(value = "获取购物车满足条件的促销与优惠券信息")
	@PostMapping("inner/findCartActivityAndCoupon/{userId}")
	OrderConfirmVo findCartActivityAndCoupon(@RequestBody List<CartInfo> cartInfoList, @PathVariable("userId") Long userId, HttpServletRequest request) {
		return activityInfoService.findCartActivityAndCoupon(cartInfoList, userId);
	}
}