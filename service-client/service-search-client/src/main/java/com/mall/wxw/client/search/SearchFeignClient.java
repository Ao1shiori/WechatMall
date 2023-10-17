package com.mall.wxw.client.search;

import com.mall.wxw.model.search.SkuEs;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * @author: wxw24633
 * @Time: 2023/10/17  12:55
 */
@FeignClient(value = "service-search")
public interface SearchFeignClient {
    //获取爆款商品
    @ApiOperation(value = "获取爆品商品")
    @GetMapping("/api/search/sku/inner/findHotSkuList")
    public List<SkuEs> findHotSkuList();
}
