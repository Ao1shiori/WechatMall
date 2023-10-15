package com.mall.wxw.sys.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.wxw.model.sys.Region;

import java.util.List;

/**
 * <p>
 * 地区表 服务类
 * </p>
 *
 * @author wxw
 * @since 2023-10-15
 */
public interface RegionService extends IService<Region> {

    List<Region> getRegionByKeyword(String keyword);
}
