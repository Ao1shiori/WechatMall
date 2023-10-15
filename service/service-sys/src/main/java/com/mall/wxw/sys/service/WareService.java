package com.mall.wxw.sys.service;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.wxw.model.sys.Ware;
import com.mall.wxw.vo.product.WareQueryVo;

/**
 * <p>
 * 仓库表 服务类
 * </p>
 *
 * @author wxw
 * @since 2023-10-15
 */
public interface WareService extends IService<Ware> {

    IPage<Ware> selectWarePage(Page<Ware> pageParam, WareQueryVo wareQueryVo);
}
