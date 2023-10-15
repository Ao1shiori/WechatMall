package com.mall.wxw.product.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.wxw.model.product.Attr;

import java.util.List;

/**
 * <p>
 * 商品属性 服务类
 * </p>
 *
 * @author wxw
 * @since 2023-10-15
 */
public interface AttrService extends IService<Attr> {

    List<Attr> findByAttrGroupId(Long attrGroupId);
}
