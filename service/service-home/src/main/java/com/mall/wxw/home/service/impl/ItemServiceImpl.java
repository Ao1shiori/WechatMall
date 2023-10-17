package com.mall.wxw.home.service.impl;

import com.mall.wxw.client.activity.ActivityFeignClient;
import com.mall.wxw.client.product.ProductFeignClient;
import com.mall.wxw.client.search.SearchFeignClient;
import com.mall.wxw.home.service.ItemService;
import com.mall.wxw.vo.product.SkuInfoVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author: wxw24633
 * @Time: 2023/10/17  14:54
 */
@Service
public class ItemServiceImpl implements ItemService {

    @Resource
    private ProductFeignClient productFeignClient;

    @Resource
    private ActivityFeignClient activityFeignClient;

    @Resource
    private SearchFeignClient searchFeignClient;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @Override
    public Map<String, Object> item(Long skuId, Long userId) {
        Map<String, Object> result = new HashMap<>();
        //skuid查询
        CompletableFuture<SkuInfoVo> skuInfoVoCompletableFuture = CompletableFuture.supplyAsync(() -> {
            //远程调用
            SkuInfoVo skuInfoVo = productFeignClient.getSkuInfoVo(skuId);
            result.put("skuInfoVo",skuInfoVo);
            return skuInfoVo;
        },threadPoolExecutor);
        //sku对应优惠券信息
        CompletableFuture<Void> activityCompletableFuture = CompletableFuture.runAsync(() -> {
            //远程调用
            Map<String,Object> activityMap = activityFeignClient.findActivityAndCoupon(skuId,userId);
            result.putAll(activityMap);
        },threadPoolExecutor);
        //更新商品热度
        CompletableFuture<Void> hotCompletableFuture = CompletableFuture.runAsync(() -> {
            //rpc更新热度
            searchFeignClient.incrHotScore(skuId);
        },threadPoolExecutor);
        //任务组合
        CompletableFuture.allOf(
                skuInfoVoCompletableFuture,
                activityCompletableFuture,
                hotCompletableFuture).join();
        return result;
    }
}
