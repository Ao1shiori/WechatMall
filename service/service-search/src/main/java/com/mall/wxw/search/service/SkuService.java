package com.mall.wxw.search.service;

import com.mall.wxw.model.search.SkuEs;

import java.util.List;

/**
 * @author: wxw24633
 * @Time: 2023/10/16  13:20
 */
public interface SkuService {
    void upperSku(Long skuId);

    void lowerSku(Long skuId);

    List<SkuEs> findHotSkuList();
}
