package com.mall.wxw.activity.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.wxw.model.activity.CouponInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 * 优惠券信息 Mapper 接口
 * </p>
 *
 * @author wxw
 * @since 2023-10-16
 */
@Repository
public interface CouponInfoMapper extends BaseMapper<CouponInfo> {

    List<CouponInfo> selectCouponInfoList(@Param("skuId") Long skuId,
                                          @Param("categoryId") Long categoryId,
                                          @Param("userId") Long userId);
}
