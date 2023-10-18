package com.mall.wxw.order.client;

import com.mall.wxw.model.order.OrderInfo;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author: wxw24633
 * @Time: 2023/10/18  16:01
 */
@FeignClient(value = "service-order")
public interface OrderFeignClient {
    @ApiOperation("获取订单详情")
    @GetMapping("/api/order/inner/getOrderInfo/{orderNo}")
    public OrderInfo getOrderInfo(@PathVariable String orderNo);
}
