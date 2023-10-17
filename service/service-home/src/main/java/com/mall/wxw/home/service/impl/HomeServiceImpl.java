package com.mall.wxw.home.service.impl;

import com.mall.wxw.client.product.ProductFeignClient;
import com.mall.wxw.client.search.SearchFeignClient;
import com.mall.wxw.client.user.UserFeignClient;
import com.mall.wxw.home.service.HomeService;
import com.mall.wxw.model.product.Category;
import com.mall.wxw.model.product.SkuInfo;
import com.mall.wxw.model.search.SkuEs;
import com.mall.wxw.vo.user.LeaderAddressVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: wxw24633
 * @Time: 2023/10/17  12:07
 */
@Service
public class HomeServiceImpl implements HomeService {
    @Resource
    private UserFeignClient userFeignClient;

    @Resource
    private ProductFeignClient productFeignClient;

    @Resource
    private SearchFeignClient searchFeignClient;

    @Override
    public Map<String, Object> home(Long userId) {
        Map<String,Object> result = new HashMap<>();
        //userId获取提货地址信息 rpc userService获取数据
        LeaderAddressVo leaderAddressVo = userFeignClient.getUserAddressByUserId(userId);
        result.put("leaderAddressVo",leaderAddressVo);
        //分类信息 rpc productService
        List<Category> categoryList = productFeignClient.findAllCategoryList();
        result.put("categoryList",categoryList);
        //新人专享信息 rpc productService
        List<SkuInfo> newPersonSkuInfoList = productFeignClient.findNewPersonSkuInfoList();
        result.put("newPersonSkuInfoList",newPersonSkuInfoList);
        //爆款商品信息 rpc searchService score评分降序
        List<SkuEs> hotSkuList = searchFeignClient.findHotSkuList();
        result.put("hotSkuList",hotSkuList);
        //封装数据返回
        return result;
    }
}
