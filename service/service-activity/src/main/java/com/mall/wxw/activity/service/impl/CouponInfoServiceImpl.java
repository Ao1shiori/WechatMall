package com.mall.wxw.activity.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.wxw.activity.mapper.CouponInfoMapper;
import com.mall.wxw.activity.mapper.CouponRangeMapper;
import com.mall.wxw.activity.mapper.CouponUseMapper;
import com.mall.wxw.activity.service.CouponInfoService;
import com.mall.wxw.client.product.ProductFeignClient;
import com.mall.wxw.enums.CouponRangeType;
import com.mall.wxw.model.activity.CouponInfo;
import com.mall.wxw.model.activity.CouponRange;
import com.mall.wxw.model.order.CartInfo;
import com.mall.wxw.model.product.Category;
import com.mall.wxw.model.product.SkuInfo;
import com.mall.wxw.vo.activity.CouponRuleVo;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 优惠券信息 服务实现类
 * </p>
 *
 * @author wxw
 * @since 2023-10-16
 */
@Service
public class CouponInfoServiceImpl extends ServiceImpl<CouponInfoMapper, CouponInfo> implements CouponInfoService {

    @Resource
    private CouponInfoMapper couponInfoMapper;

    @Resource
    private CouponRangeMapper couponRangeMapper;

    @Resource
    private CouponUseMapper couponUseMapper;

    @Resource
    private ProductFeignClient productFeignClient;

    //优惠卷分页查询
    @Override
    public IPage<CouponInfo> selectPage(Page<CouponInfo> pageParam) {
        //  构造排序条件
        QueryWrapper<CouponInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("id");
        IPage<CouponInfo> page = couponInfoMapper.selectPage(pageParam, queryWrapper);
        page.getRecords().stream().forEach(item -> {
            item.setCouponTypeString(item.getCouponType().getComment());
            if(item.getRangeType() != null) {
                item.setRangeTypeString(item.getRangeType().getComment());
            }
        });
        //  返回数据集合
        return page;
    }

    //根据id获取优惠券
    @Override
    public CouponInfo getCouponInfo(String id) {
        CouponInfo couponInfo = this.getById(id);
        couponInfo.setCouponTypeString(couponInfo.getCouponType().getComment());
        if(null != couponInfo.getRangeType()) {
            couponInfo.setRangeTypeString(couponInfo.getRangeType().getComment());
        }
        return couponInfo;
    }

    //根据优惠卷id获取优惠券规则列表
    @Override
    public Map<String, Object> findCouponRuleList(Long couponId) {
        Map<String, Object> result = new HashMap<>();
        //id查优惠券基本信息
        CouponInfo couponInfo = this.getById(couponId);
        //id查对应的优惠券范围列表
        List<CouponRange> activitySkuList =
                couponRangeMapper.selectList(new QueryWrapper<CouponRange>().eq("coupon_id",couponId));
        //优惠券范围id列表
        List<Long> rangeIdList = activitySkuList.stream().map(CouponRange::getRangeId).collect(Collectors.toList());

        if(!CollectionUtils.isEmpty(rangeIdList)) {
            if(couponInfo.getRangeType() == CouponRangeType.SKU) {
                List<SkuInfo> skuInfoList = productFeignClient.findSkuInfoList(rangeIdList);
                result.put("skuInfoList", skuInfoList);

            } else if (couponInfo.getRangeType() == CouponRangeType.CATEGORY) {
                List<Category> categoryList = productFeignClient.findCategoryList(rangeIdList);
                result.put("categoryList", categoryList);

            } else {
                //通用
            }
        }
        return result;
    }

    //新增优惠券规则
    @Override
    public void saveCouponRule(CouponRuleVo couponRuleVo) {
        /*
        优惠券couponInfo 与 couponRange 要一起操作：先删除couponRange ，更新couponInfo ，再新增couponRange ！
         */
        couponRangeMapper
                .delete(new QueryWrapper<CouponRange>().eq("coupon_id",couponRuleVo.getCouponId()));
        //  更新数据
        CouponInfo couponInfo = baseMapper.selectById(couponRuleVo.getCouponId());
        // couponInfo.setCouponType();
        couponInfo.setRangeType(couponRuleVo.getRangeType());
        couponInfo.setConditionAmount(couponRuleVo.getConditionAmount());
        couponInfo.setAmount(couponRuleVo.getAmount());
        couponInfo.setConditionAmount(couponRuleVo.getConditionAmount());
        couponInfo.setRangeDesc(couponRuleVo.getRangeDesc());
        baseMapper.updateById(couponInfo);
        //  插入优惠券的规则 couponRangeList
        List<CouponRange> couponRangeList = couponRuleVo.getCouponRangeList();
        for (CouponRange couponRange : couponRangeList) {
            couponRange.setCouponId(couponRuleVo.getCouponId());
            //  插入数据
            couponRangeMapper.insert(couponRange);
        }
    }

    //根据关键字获取sku列表，活动使用
    @Override
    public List<CouponInfo> findCouponByKeyword(String keyword) {
        //  模糊查询
        QueryWrapper<CouponInfo> couponInfoQueryWrapper = new QueryWrapper<>();
        couponInfoQueryWrapper.like("coupon_name",keyword);
        return couponInfoMapper.selectList(couponInfoQueryWrapper);
    }

    @Override
    public List<CouponInfo> findCouponInfoList(Long skuId, Long userId) {
        //skuid获取skuInfo
        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
        //条件查询skuid 分类id userid
        return baseMapper.selectCouponInfoList(skuId,skuInfo.getCategoryId(),userId);
    }

    //购物车可用优惠券列表
    @Override
    public List<CouponInfo> findCartCouponInfo(List<CartInfo> cartInfoList, Long userId) {
        List<CouponInfo> couponInfoList = new ArrayList<>();
        //userid获取用户全部优惠券列表
        List<CouponInfo> userAllCouponInfoList = baseMapper.selectCartCouponInfoList(userId);
        if (CollectionUtils.isEmpty(userAllCouponInfoList)){
            return new ArrayList<>();
        }
        //获取优惠券id列表
        List<Long> couponIdList = userAllCouponInfoList.stream().map(CouponInfo::getId).collect(Collectors.toList());
        //查询优惠券对应范围
        List<CouponRange> couponRangeList = couponRangeMapper
                .selectList(new LambdaQueryWrapper<CouponRange>().in(CouponRange::getCouponId, couponIdList));
        //获取优惠券id对应的满足使用范围的购物项skuId列表
        Map<Long, List<Long>> couponIdToSkuIdMap = this.findCouponIdToSkuIdMap(cartInfoList, couponRangeList);
        //优惠后减少金额
        BigDecimal reduceAmount = new BigDecimal("0");

        //记录最优优惠券
        CouponInfo optimalCouponInfo = null;
        for(CouponInfo couponInfo : userAllCouponInfoList) {
            if(CouponRangeType.ALL == couponInfo.getRangeType()) {
                //全场通用
                //判断是否满足优惠使用门槛
                //计算购物车商品的总价
                BigDecimal totalAmount = computeTotalAmount(cartInfoList);
                if(totalAmount.subtract(couponInfo.getConditionAmount()).doubleValue() >= 0){
                    couponInfo.setIsSelect(1);
                }
            } else {
                //优惠券id对应的满足使用范围的购物项skuId列表
                List<Long> skuIdList = couponIdToSkuIdMap.get(couponInfo.getId());
                //当前满足使用范围的购物项
                List<CartInfo> currentCartInfoList = cartInfoList.stream().filter(cartInfo -> skuIdList.contains(cartInfo.getSkuId())).collect(Collectors.toList());
                BigDecimal totalAmount = computeTotalAmount(currentCartInfoList);
                if(totalAmount.subtract(couponInfo.getConditionAmount()).doubleValue() >= 0){
                    couponInfo.setIsSelect(1);
                }
            }
            if (couponInfo.getIsSelect() == 1 && couponInfo.getAmount().subtract(reduceAmount).doubleValue() > 0) {
                reduceAmount = couponInfo.getAmount();
                optimalCouponInfo = couponInfo;
            }
        }
        if(null != optimalCouponInfo) {
            optimalCouponInfo.setIsOptimal(1);
        }
        return userAllCouponInfoList;
    }

    private BigDecimal computeTotalAmount(List<CartInfo> cartInfoList) {
        BigDecimal total = new BigDecimal("0");
        for (CartInfo cartInfo : cartInfoList) {
            //是否选中
            if(cartInfo.getIsChecked() == 1) {
                BigDecimal itemTotal = cartInfo.getCartPrice().multiply(new BigDecimal(cartInfo.getSkuNum()));
                total = total.add(itemTotal);
            }
        }
        return total;
    }

    private Map<Long, List<Long>> findCouponIdToSkuIdMap(List<CartInfo> cartInfoList, List<CouponRange> couponRangeList) {
        Map<Long, List<Long>> couponIdToSkuIdMap = new HashMap<>();
        //根据优惠券id分组
        Map<Long, List<CouponRange>> couponIdToCouponRangeListMap = couponRangeList
                .stream()
                .collect(Collectors.groupingBy(CouponRange::getCouponId));
        for (Map.Entry<Long, List<CouponRange>> entry : couponIdToCouponRangeListMap.entrySet()) {
            Long couponId = entry.getKey();
            List<CouponRange> rangeList = entry.getValue();

            Set<Long> skuIdSet = new HashSet<>();
            for (CartInfo cartInfo : cartInfoList) {
                for (CouponRange couponRange : rangeList) {
                    if (CouponRangeType.SKU == couponRange.getRangeType() && couponRange.getRangeId() == cartInfo.getSkuId().intValue()) {
                        skuIdSet.add(cartInfo.getSkuId());
                    } else if (CouponRangeType.CATEGORY == couponRange.getRangeType() && couponRange.getRangeId() == cartInfo.getCategoryId().intValue()) {
                        skuIdSet.add(cartInfo.getSkuId());
                    } else {

                    }
                }
            }
            couponIdToSkuIdMap.put(couponId, new ArrayList<>(skuIdSet));
        }

        return couponIdToSkuIdMap;
    }
}
