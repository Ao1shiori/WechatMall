package com.mall.wxw.sys.service.impl;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.wxw.model.sys.Ware;
import com.mall.wxw.sys.mapper.WareMapper;
import com.mall.wxw.sys.service.WareService;
import com.mall.wxw.vo.product.WareQueryVo;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 仓库表 服务实现类
 * </p>
 *
 * @author wxw
 * @since 2023-10-15
 */
@Service
public class WareServiceImpl extends ServiceImpl<WareMapper, Ware> implements WareService {

    @Override
    public IPage<Ware> selectWarePage(Page<Ware> pageParam, WareQueryVo wareQueryVo) {
        return null;
    }
}
