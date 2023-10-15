package com.mall.wxw.product.service;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.wxw.model.product.AttrGroup;
import com.mall.wxw.vo.product.AttrGroupQueryVo;

import java.util.List;

/**
 * <p>
 * 属性分组 服务类
 * </p>
 *
 * @author wxw
 * @since 2023-10-15
 */
public interface AttrGroupService extends IService<AttrGroup> {

    IPage<AttrGroup> selectAttrGroupPage(Page<AttrGroup> pageParam, AttrGroupQueryVo attrGroupQueryVo);

    List<AttrGroup> findAllList();
}
