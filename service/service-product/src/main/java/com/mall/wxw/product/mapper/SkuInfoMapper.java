package com.mall.wxw.product.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.wxw.model.product.SkuInfo;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * sku信息 Mapper 接口
 * </p>
 *
 * @author wxw
 * @since 2023-10-15
 */
public interface SkuInfoMapper extends BaseMapper<SkuInfo> {

    void unlockStock(@Param("skuId") Long skuId,@Param("skuNum") Integer skuNum);

    SkuInfo checkStock(@Param("skuId")Long skuId,@Param("skuNum") Integer skuNum);

    Integer lockStock(@Param("skuId")Long skuId,@Param("skuNum") Integer skuNum);
}
