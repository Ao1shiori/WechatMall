package com.mall.wxw.product.service;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.wxw.model.product.Category;
import com.mall.wxw.vo.product.CategoryQueryVo;

import java.util.List;

/**
 * <p>
 * 商品三级分类 服务类
 * </p>
 *
 * @author wxw
 * @since 2023-10-15
 */
public interface CategoryService extends IService<Category> {

    IPage<Category> selectCategoryPage(Page<Category> pageParam, CategoryQueryVo categoryQueryVo);

    //查询所有商品分类
    List<Category> findAllList();
}
