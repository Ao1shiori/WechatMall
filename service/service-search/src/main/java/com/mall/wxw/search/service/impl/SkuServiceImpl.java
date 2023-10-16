package com.mall.wxw.search.service.impl;

import com.mall.wxw.client.product.ProductFeignClient;
import com.mall.wxw.enums.SkuType;
import com.mall.wxw.model.product.Category;
import com.mall.wxw.model.product.SkuInfo;
import com.mall.wxw.model.search.SkuEs;
import com.mall.wxw.search.repository.SkuRepository;
import com.mall.wxw.search.service.SkuService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author: wxw24633
 * @Time: 2023/10/16  13:20
 */
@Service
public class SkuServiceImpl implements SkuService {
    @Resource
    private SkuRepository skuRepository;

    @Resource
    private ProductFeignClient productFeignClient;

    @Override
    public void upperSku(Long skuId) {
        //查询sku信息 分类
        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
        if (skuInfo == null){
            return;
        }
        Category category = productFeignClient.getCategory(skuInfo.getCategoryId());
        //封装对象
        SkuEs skuEs = new SkuEs();
        if (category != null) {
            skuEs.setCategoryId(category.getId());
            skuEs.setCategoryName(category.getName());
        }
        skuEs.setId(skuInfo.getId());
        skuEs.setKeyword(skuInfo.getSkuName()+","+skuEs.getCategoryName());
        skuEs.setWareId(skuInfo.getWareId());
        skuEs.setIsNewPerson(skuInfo.getIsNewPerson());
        skuEs.setImgUrl(skuInfo.getImgUrl());
        skuEs.setTitle(skuInfo.getSkuName());
        if(skuInfo.getSkuType() == SkuType.COMMON.getCode()) {
            skuEs.setSkuType(0);
            skuEs.setPrice(skuInfo.getPrice().doubleValue());
            skuEs.setStock(skuInfo.getStock());
            skuEs.setSale(skuInfo.getSale());
            skuEs.setPerLimit(skuInfo.getPerLimit());
        }
        //添加到ES
        skuRepository.save(skuEs);
    }

    @Override
    public void lowerSku(Long skuId) {
        skuRepository.deleteById(skuId);
    }
}
