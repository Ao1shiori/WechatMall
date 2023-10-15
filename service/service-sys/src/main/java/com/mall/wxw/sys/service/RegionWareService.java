package com.mall.wxw.sys.service;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.wxw.model.sys.RegionWare;
import com.mall.wxw.vo.sys.RegionWareQueryVo;

/**
 * <p>
 * 城市仓库关联表 服务类
 * </p>
 *
 * @author wxw
 * @since 2023-10-15
 */
public interface RegionWareService extends IService<RegionWare> {

    IPage<RegionWare> selectRegionWarePage(Page<RegionWare> pageParam, RegionWareQueryVo regionWareQueryVo);

    void saveRegionWare(RegionWare regionWare);
}
