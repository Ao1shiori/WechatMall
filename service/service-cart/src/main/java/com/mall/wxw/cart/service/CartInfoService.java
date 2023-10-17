package com.mall.wxw.cart.service;

import java.util.List;

/**
 * @author: wxw24633
 * @Time: 2023/10/17  16:18
 */
public interface CartInfoService {
    void addToCart(Long userId, Long skuId, Integer skuNum);

    void deleteCart(Long skuId, Long userId);

    void deleteAllCart(Long userId);

    void batchDeleteCart(List<Long> skuIdList, Long userId);
}
