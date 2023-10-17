package com.mall.wxw.activity.api;

import com.mall.wxw.activity.service.ActivityInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
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
}