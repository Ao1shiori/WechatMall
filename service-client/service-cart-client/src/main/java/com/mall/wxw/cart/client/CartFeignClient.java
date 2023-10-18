package com.mall.wxw.cart.client;

import com.mall.wxw.model.order.CartInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * @author: wxw24633
 * @Time: 2023/10/18  11:37
 */
@FeignClient(value = "service-cart")
public interface CartFeignClient {

    /**
     * 根据用户Id 查询选中购物车列表
     * @param userId
     */
    @GetMapping("/api/cart/inner/getCartCheckedList/{userId}")
    List<CartInfo> getCartCheckedList(@PathVariable("userId") Long userId);


}
