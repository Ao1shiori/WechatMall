package com.mall.wxw.product.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.wxw.model.product.Category;
import com.mall.wxw.product.mapper.CategoryMapper;
import com.mall.wxw.product.service.CategoryService;
import com.mall.wxw.vo.product.CategoryQueryVo;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * <p>
 * 商品三级分类 服务实现类
 * </p>
 *
 * @author wxw
 * @since 2023-10-15
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Override
    public IPage<Category> selectCategoryPage(Page<Category> pageParam, CategoryQueryVo categoryQueryVo) {
        String name = categoryQueryVo.getName();
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        if (!StringUtils.isEmpty(name)){
            wrapper.like(Category::getName,name);
        }
        return baseMapper.selectPage(pageParam,wrapper);
    }

    //查询所有商品分类
    @Override
    public List<Category> findAllList() {
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(Category::getSort);
        return this.list(queryWrapper);
    }


}
