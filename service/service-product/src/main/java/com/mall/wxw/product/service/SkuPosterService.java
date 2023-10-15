package com.mall.wxw.product.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.wxw.model.product.SkuPoster;

import java.util.List;

/**
 * <p>
 * 商品海报表 服务类
 * </p>
 *
 * @author wxw
 * @since 2023-10-15
 */
public interface SkuPosterService extends IService<SkuPoster> {

    List<SkuPoster> getPosterListBySkuId(Long id);
}
