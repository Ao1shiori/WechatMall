package com.mall.wxw.search.service.impl;

import com.mall.wxw.client.activity.ActivityFeignClient;
import com.mall.wxw.client.product.ProductFeignClient;
import com.mall.wxw.common.auth.AuthContextHolder;
import com.mall.wxw.enums.SkuType;
import com.mall.wxw.model.product.Category;
import com.mall.wxw.model.product.SkuInfo;
import com.mall.wxw.model.search.SkuEs;
import com.mall.wxw.search.repository.SkuRepository;
import com.mall.wxw.search.service.SkuService;
import com.mall.wxw.vo.search.SkuEsQueryVo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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

    @Resource
    private ActivityFeignClient activityFeignClient;

    @Resource
    private RedisTemplate redisTemplate;

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

    @Override
    public List<SkuEs> findHotSkuList() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<SkuEs> pageModel = skuRepository.findByOrderByHotScoreDesc(pageable);
        return pageModel.getContent();
    }

    @Override
    public Page<SkuEs> search(Pageable pageable, SkuEsQueryVo skuEsQueryVo) {
        skuEsQueryVo.setWareId(AuthContextHolder.getWareId());
        Page<SkuEs> page;
        if(StringUtils.isEmpty(skuEsQueryVo.getKeyword())) {
            page = skuRepository.findByCategoryIdAndWareId(skuEsQueryVo.getCategoryId(), skuEsQueryVo.getWareId(), pageable);
        } else {
            page = skuRepository.findByKeywordAndWareId(skuEsQueryVo.getKeyword(), skuEsQueryVo.getWareId(), pageable);
        }
        List<SkuEs> skuEsList =  page.getContent();
        //获取sku对应的促销活动标签
        if(!CollectionUtils.isEmpty(skuEsList)) {
            List<Long> skuIdList = skuEsList.stream().map(sku -> sku.getId()).collect(Collectors.toList());
            //一个商品参加的活动会有多个规则
            Map<Long, List<String>> skuIdToRuleListMap = activityFeignClient.findActivity(skuIdList);
            //封装规则到skuEs中
            if(null != skuIdToRuleListMap) {
                skuEsList.forEach(skuEs -> {
                    skuEs.setRuleList(skuIdToRuleListMap.get(skuEs.getId()));
                });
            }
        }
        return page;
    }

    //更新商品热度
    @Override
    public void incrHotScore(Long skuId) {
        // 定义key
        String hotKey = "hotScore";
        // 保存数据 每次加1
        Double hotScore = redisTemplate.opsForZSet().incrementScore(hotKey, "skuId:" + skuId, 1);
        if (hotScore%10==0){
            // 更新es
            Optional<SkuEs> optional = skuRepository.findById(skuId);
            SkuEs skuEs = optional.get();
            skuEs.setHotScore(skuEs.getHotScore() + Math.round(hotScore));
            skuRepository.save(skuEs);
        }
    }
}
