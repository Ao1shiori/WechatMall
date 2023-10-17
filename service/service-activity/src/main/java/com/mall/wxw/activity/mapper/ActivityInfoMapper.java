package com.mall.wxw.activity.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.wxw.model.activity.ActivityInfo;
import com.mall.wxw.model.activity.ActivityRule;
import com.mall.wxw.model.activity.ActivitySku;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 * 活动表 Mapper 接口
 * </p>
 *
 * @author wxw
 * @since 2023-10-16
 */
@Repository
public interface ActivityInfoMapper extends BaseMapper<ActivityInfo> {

    List<Long> selectSkuIdListExist(@Param("skuIdList") List<Long> skuIdList);

    List<ActivityRule> findActivityRule(@Param("skuId") Long skuId);

    List<ActivitySku> selectCartActivity(@Param("skuIdList")List<Long> skuIdList);
}
