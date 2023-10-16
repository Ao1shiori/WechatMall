package com.mall.wxw.product.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.wxw.common.result.Result;
import com.mall.wxw.model.product.Category;
import com.mall.wxw.product.service.CategoryService;
import com.mall.wxw.vo.product.CategoryQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 商品三级分类 前端控制器
 * </p>
 *
 * @author wxw
 * @since 2023-10-15
 */
@RestController
@RequestMapping("/admin/product/category")
@Api(tags = "商品分类接口")
public class CategoryController {
    @Resource
    private CategoryService categoryService;

    @ApiOperation("条件分页查询商品分类")
    @GetMapping("/{page}/{limit}")
    public Result getPageList(@PathVariable Long page,
                              @PathVariable Long limit,
                              CategoryQueryVo categoryQueryVo){
        Page<Category> pageParam = new Page<>(page, limit);
        IPage<Category> pageModel = categoryService.selectCategoryPage(pageParam,categoryQueryVo);
        return Result.ok(pageModel);
    }

    @ApiOperation("获取商品分类信息")
    @GetMapping("get/{id}")
    public Result get(@PathVariable Long id){
        return Result.ok(categoryService.getById(id));
    }

    @ApiOperation("新增商品分类")
    @PostMapping("save")
    public Result save(@RequestBody Category category){
        categoryService.save(category);
        return Result.ok(null);
    }

    @ApiOperation("修改商品分类")
    @PutMapping("update")
    public Result update(@RequestBody Category category){
        categoryService.updateById(category);
        return Result.ok(null);
    }

    @ApiOperation("删除商品分类")
    @DeleteMapping("remove/{id}")
    public Result remove(@PathVariable Long id){
        categoryService.removeById(id);
        return Result.ok(null);
    }

    @ApiOperation("根据id列表删除商品分类")
    @DeleteMapping("batchRemove")
    public Result batchRemove(@RequestBody List<Long> idList){
        categoryService.removeByIds(idList);
        return Result.ok(null);
    }

    @ApiOperation("获取全部商品分类")
    @GetMapping("findAllList")
    public Result findAllList(){
        return Result.ok(categoryService.findAllList());
    }

}

