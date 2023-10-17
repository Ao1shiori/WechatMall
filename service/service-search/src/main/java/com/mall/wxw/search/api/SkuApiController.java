package com.mall.wxw.search.api;

import com.mall.wxw.model.search.SkuEs;
import com.mall.wxw.search.service.SkuService;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author: wxw24633
 * @Time: 2023/10/17  12:44
 */

@RestController
@RequestMapping("/api/search/sku")
public class SkuApiController {
    @Resource
    private SkuService skuService;

    //获取爆款商品
    @ApiOperation(value = "获取爆品商品")
    @GetMapping("inner/findHotSkuList")
    public List<SkuEs> findHotSkuList() {
        return skuService.findHotSkuList();
    }
}
