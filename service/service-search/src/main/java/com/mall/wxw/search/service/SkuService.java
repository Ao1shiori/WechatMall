package com.mall.wxw.search.service;

import com.mall.wxw.model.search.SkuEs;
import com.mall.wxw.vo.search.SkuEsQueryVo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * @author: wxw24633
 * @Time: 2023/10/16  13:20
 */
public interface SkuService {
    void upperSku(Long skuId);

    void lowerSku(Long skuId);

    List<SkuEs> findHotSkuList();

    //分类信息
    Page<SkuEs> search(Pageable pageable, SkuEsQueryVo skuEsQueryVo);

    void incrHotScore(Long skuId);
}
