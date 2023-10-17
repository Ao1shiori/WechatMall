package com.mall.wxw.activity.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.wxw.activity.mapper.ActivityInfoMapper;
import com.mall.wxw.activity.mapper.ActivityRuleMapper;
import com.mall.wxw.activity.mapper.ActivitySkuMapper;
import com.mall.wxw.activity.service.ActivityInfoService;
import com.mall.wxw.activity.service.CouponInfoService;
import com.mall.wxw.client.product.ProductFeignClient;
import com.mall.wxw.enums.ActivityType;
import com.mall.wxw.model.activity.ActivityInfo;
import com.mall.wxw.model.activity.ActivityRule;
import com.mall.wxw.model.activity.ActivitySku;
import com.mall.wxw.model.activity.CouponInfo;
import com.mall.wxw.model.order.CartInfo;
import com.mall.wxw.model.product.SkuInfo;
import com.mall.wxw.vo.activity.ActivityRuleVo;
import com.mall.wxw.vo.order.CartInfoVo;
import com.mall.wxw.vo.order.OrderConfirmVo;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 活动表 服务实现类
 * </p>
 *
 * @author wxw
 * @since 2023-10-16
 */
@Service
public class ActivityInfoServiceImpl extends ServiceImpl<ActivityInfoMapper, ActivityInfo> implements ActivityInfoService {

    @Resource
    private ActivityInfoMapper activityInfoMapper;

    @Resource
    private ActivityRuleMapper activityRuleMapper;

    @Resource
    private ActivitySkuMapper activitySkuMapper;

    @Resource
    private ProductFeignClient productFeignClient;

    @Resource
    private CouponInfoService couponInfoService;

    @Override
    public IPage<ActivityInfo> selectPage(Page<ActivityInfo> pageParam) {
        QueryWrapper<ActivityInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("id");

        IPage<ActivityInfo> page = activityInfoMapper.selectPage(pageParam, queryWrapper);
        page.getRecords().stream().forEach(item -> {
            item.setActivityTypeString(item.getActivityType().getComment());
        });
        return page;
    }

    @Override
    public Map<String, Object> findActivityRuleList(Long activityId) {
        Map<String, Object> result = new HashMap<>();

        QueryWrapper<ActivityRule> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("activity_id",activityId);
        List<ActivityRule> activityRuleList = activityRuleMapper.selectList(queryWrapper);

        //处理规则名称
        List<String> ruleList = new ArrayList<>();
        for (ActivityRule activityRule : activityRuleList) {
            ruleList.add(getRuleDesc(activityRule));
        }
        result.put("activityRuleList", ruleList);

        List<ActivitySku> activitySkuList = activitySkuMapper.selectList
                (new QueryWrapper<ActivitySku>().eq("activity_id",activityId));
        //skuId远程调用获取信息
        List<Long> skuIdList = activitySkuList.stream().map(ActivitySku::getSkuId).collect(Collectors.toList());
        List<SkuInfo> skuInfoList = productFeignClient.findSkuInfoList(skuIdList);
        result.put("skuInfoList", skuInfoList);

        return result;
    }

    @Override
    public void saveActivityRule(ActivityRuleVo activityRuleVo) {
        Long activityId = activityRuleVo.getActivityId();
        //ActivityRule数据删除
        activityRuleMapper.delete(new LambdaQueryWrapper<ActivityRule>()
                .eq(ActivityRule::getActivityId,activityId));
        //ActivitySku数据删除
        activitySkuMapper.delete(new LambdaQueryWrapper<ActivitySku>()
                .eq(ActivitySku::getActivityId,activityId));
        //规则列表数据封装
        List<ActivityRule> activityRuleList = activityRuleVo.getActivityRuleList();
        ActivityInfo activityInfo = activityInfoMapper.selectById(activityRuleVo.getActivityId());
        for(ActivityRule activityRule : activityRuleList) {
            activityRule.setActivityId(activityRuleVo.getActivityId());
            activityRule.setActivityType(activityInfo.getActivityType());
            activityRuleMapper.insert(activityRule);
        }
        //规则范围数据
        List<ActivitySku> activitySkuList = activityRuleVo.getActivitySkuList();
        for(ActivitySku activitySku : activitySkuList) {
            activitySku.setActivityId(activityRuleVo.getActivityId());
            activitySkuMapper.insert(activitySku);
        }
    }

    @Override
    public List<SkuInfo> findSkuInfoByKeyword(String keyword) {
        //根据关键字查询sku匹配内容列表
        List<SkuInfo> skuInfoList = productFeignClient.findSkuInfoByKeyword(keyword);
        //查不到匹配内容返回空集合
        if (skuInfoList.size()==0){
            return skuInfoList;
        }
        List<Long> skuIdList = skuInfoList.stream().map(SkuInfo::getId).collect(Collectors.toList());

        List<Long> existSkuIdList = baseMapper.selectSkuIdListExist(skuIdList);

        List<SkuInfo> notExistSkuInfoList = new ArrayList<>();
        //排除已经存在的sku
        for(SkuInfo skuInfo : skuInfoList) {
            if(existSkuIdList.contains(skuInfo.getId())) {
                notExistSkuInfoList.add(skuInfo);
            }
        }
        return notExistSkuInfoList;
    }

    @Override
    public List<ActivityRule> findActivityRule(Long skuId) {
        return baseMapper.findActivityRule(skuId);
    }

    @Override
    public Map<Long, List<String>> findActivity(List<Long> skuIdList) {
        Map<Long, List<String>> result = new HashMap<>();
        //遍历skuIdList
        skuIdList.forEach(skuId ->{
            //查询skuId对应活动的规则列表
            List<ActivityRule> activityRuleList = baseMapper.findActivityRule(skuId);
            //封装数据
            if (!CollectionUtils.isEmpty(activityRuleList)){
                List<String> ruleList = new ArrayList<>();
                //处理规则名称
                for (ActivityRule activityRule : activityRuleList) {
                    ruleList.add(getRuleDesc(activityRule));
                }
                result.put(skuId,ruleList);
            }
        });
        return result;
    }

    @Override
    public Map<String, Object> findActivityAndCoupon(Long skuId, Long userId) {
        ActivitySku activitySku = activitySkuMapper.selectOne(new LambdaQueryWrapper<ActivitySku>().eq(ActivitySku::getSkuId, skuId));
        //skuid获取sku营销活动 一个活动多个规则
        Map<String, Object> activityRuleList = this.findActivityRuleList(activitySku.getActivityId());
        //skuid和userid查优惠券信息
        List<CouponInfo> couponInfoList = couponInfoService.findCouponInfoList(skuId,userId);
        //封装返回
        Map<String, Object> result = new HashMap<>(activityRuleList);
        result.put("couponInfoList",couponInfoList);
        return result;
    }

    @Override
    public OrderConfirmVo findCartActivityAndCoupon(List<CartInfo> cartInfoList, Long userId) {
        //获取购物车每个购物项参与活动规则 根据活动规则分组skuid查活动id再获取活动信息
        //一个规则对应多个商品
        List<CartInfoVo> cartInfoVoList = findCartActivityList(cartInfoList);
        //计算参与活动后的金额
        BigDecimal activityReduceAmount = cartInfoVoList.stream()
                .filter(cartInfoVo -> cartInfoVo.getActivityRule() != null)
                .map(cartInfoVo -> cartInfoVo.getActivityRule().getReduceAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        //获取购物车可用优惠券列表
        List<CouponInfo> couponInfoList = couponInfoService.findCartCouponInfo(cartInfoList,userId);
        //计算使用优惠券后的金额 一次一张
        BigDecimal couponReduceAmount = new BigDecimal(0);
        if (!CollectionUtils.isEmpty(couponInfoList)){
             couponReduceAmount = couponInfoList.stream()
                    .filter(couponInfo -> couponInfo.getIsOptimal() == 1)
                    .map(CouponInfo::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        //计算没有参与活动和使用优惠券的原始金额
        BigDecimal originalTotalAmount = cartInfoList.stream()
                .filter(cartInfo -> cartInfo.getIsChecked() == 1)
                .map(cartInfo -> cartInfo.getCartPrice().multiply(new BigDecimal(cartInfo.getSkuNum())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        //参与活动 使用优惠券的最终总金额
        BigDecimal totalAmount = originalTotalAmount.subtract(activityReduceAmount).subtract(couponReduceAmount);
        //封装数据到CarInfoVo
        OrderConfirmVo orderTradeVo = new OrderConfirmVo();
        orderTradeVo.setCarInfoVoList(cartInfoVoList);
        orderTradeVo.setActivityReduceAmount(activityReduceAmount);
        orderTradeVo.setCouponInfoList(couponInfoList);
        orderTradeVo.setCouponReduceAmount(couponReduceAmount);
        orderTradeVo.setOriginalTotalAmount(originalTotalAmount);
        orderTradeVo.setTotalAmount(totalAmount);
        return orderTradeVo;
    }

    @Override
    public List<CartInfoVo> findCartActivityList(List<CartInfo> cartInfoList) {
        List<CartInfoVo> cartInfoVoList = new ArrayList<>();
        //获取skuidList
        List<Long> skuIdList = cartInfoList.stream().map(CartInfo::getSkuId).collect(Collectors.toList());
        //根据skuIdList获取参与活动
        List<ActivitySku> activitySkuList = baseMapper.selectCartActivity(skuIdList);
        //根据活动id对sku进行分组
        Map<Long, Set<Long>> activityIdToSkuIdListMap = activitySkuList.stream().collect(
                        Collectors.groupingBy(ActivitySku::getActivityId,
                        Collectors.mapping(ActivitySku::getSkuId, Collectors.toSet()))
        );
        //获取活动id对应规则数据
        Map<Long,List<ActivityRule>> activityIdToActivityRuleListMap = new HashMap<>();
        //所有活动id
        Set<Long> activityIdSet = activitySkuList.stream().map(ActivitySku::getActivityId).collect(Collectors.toSet());
        if(!CollectionUtils.isEmpty(activityIdSet)) {
            LambdaQueryWrapper<ActivityRule> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.orderByDesc(ActivityRule::getConditionAmount, ActivityRule::getConditionNum);
            //在所有活动id中查
            queryWrapper.in(ActivityRule::getActivityId, activityIdSet);
            List<ActivityRule> activityRuleList = activityRuleMapper.selectList(queryWrapper);
            //按活动Id分组，获取活动对应的规则
            activityIdToActivityRuleListMap = activityRuleList.stream().collect(Collectors.groupingBy(activityRule -> activityRule.getActivityId()));
        }
        //第三步：根据活动汇总购物项，相同活动的购物项为一组显示在页面，并且计算最优优惠金额
        //记录有活动的购物项skuId
        Set<Long> activitySkuIdSet = new HashSet<>();
        if (!CollectionUtils.isEmpty(activityIdToSkuIdListMap)){
            //遍历map集合
            for (Map.Entry<Long, Set<Long>> entry : activityIdToSkuIdListMap.entrySet()) {
                Long activityId = entry.getKey();
                //活动对应的sku列表
                Set<Long> currentActivitySkuIdSet = entry.getValue();
                //当前活动对应的购物项列表
                List<CartInfo> currentActivityCartInfoList =
                        cartInfoList
                                .stream()
                                .filter(cartInfo -> currentActivitySkuIdSet.contains(cartInfo.getSkuId()))
                                .collect(Collectors.toList());
                //当前活动的总金额
                BigDecimal activityTotalAmount = this.computeTotalAmount(currentActivityCartInfoList);
                //当前活动的购物项总个数
                Integer activityTotalNum = this.computeCartNum(currentActivityCartInfoList);
                //计算当前活动对应的最优规则
                //活动当前活动对应的规则
                List<ActivityRule> currentActivityRuleList = activityIdToActivityRuleListMap.get(activityId);
                ActivityType activityType = currentActivityRuleList.get(0).getActivityType();
                ActivityRule optimalActivityRule = null;
                //活动类型
                if (activityType == ActivityType.FULL_REDUCTION) {
                    optimalActivityRule = this.computeFullReduction(activityTotalAmount, currentActivityRuleList);
                } else {
                    optimalActivityRule = this.computeFullDiscount(activityTotalNum, activityTotalAmount, currentActivityRuleList);
                }
                //同一活动对应的购物项列表与对应优化规则
                CartInfoVo carInfoVo = new CartInfoVo();
                carInfoVo.setCartInfoList(currentActivityCartInfoList);
                carInfoVo.setActivityRule(optimalActivityRule);
                cartInfoVoList.add(carInfoVo);
                //记录参与了活动的
                activitySkuIdSet.addAll(currentActivitySkuIdSet);
            }
        }
        //第四步：无活动的购物项，每一项一组
        skuIdList.removeAll(activitySkuIdSet);
        if(!CollectionUtils.isEmpty(skuIdList)) {
            //获取skuId对应的购物项
            Map<Long, CartInfo> skuIdToCartInfoMap = cartInfoList.stream().collect(Collectors.toMap(CartInfo::getSkuId, CartInfo->CartInfo));
            for(Long skuId : skuIdList) {
                CartInfoVo cartInfoVo = new CartInfoVo();
                cartInfoVo.setActivityRule(null);
                List<CartInfo> currentCartInfoList = new ArrayList<>();
                currentCartInfoList.add(skuIdToCartInfoMap.get(skuId));
                cartInfoVo.setCartInfoList(currentCartInfoList);
                cartInfoVoList.add(cartInfoVo);
            }
        }
        return cartInfoVoList;
    }

    //构造规则名称的方法
    private String getRuleDesc(ActivityRule activityRule) {
        ActivityType activityType = activityRule.getActivityType();
        StringBuilder ruleDesc = new StringBuilder();
        if (activityType == ActivityType.FULL_REDUCTION) {
            ruleDesc
                    .append("满")
                    .append(activityRule.getConditionAmount())
                    .append("元减")
                    .append(activityRule.getBenefitAmount())
                    .append("元");
        } else {
            ruleDesc
                    .append("满")
                    .append(activityRule.getConditionNum())
                    .append("元打")
                    .append(activityRule.getBenefitDiscount())
                    .append("折");
        }
        return ruleDesc.toString();
    }

    /**
     * 计算满量打折最优规则
     * @param totalNum
     * @param activityRuleList //该活动规则skuActivityRuleList数据，已经按照优惠折扣从大到小排序了
     */
    private ActivityRule computeFullDiscount(Integer totalNum, BigDecimal totalAmount, List<ActivityRule> activityRuleList) {
        ActivityRule optimalActivityRule = null;
        //该活动规则skuActivityRuleList数据，已经按照优惠金额从大到小排序了
        for (ActivityRule activityRule : activityRuleList) {
            //如果订单项购买个数大于等于满减件数，则优化打折
            if (totalNum.intValue() >= activityRule.getConditionNum()) {
                BigDecimal skuDiscountTotalAmount = totalAmount.multiply(activityRule.getBenefitDiscount().divide(new BigDecimal("10")));
                BigDecimal reduceAmount = totalAmount.subtract(skuDiscountTotalAmount);
                activityRule.setReduceAmount(reduceAmount);
                optimalActivityRule = activityRule;
                break;
            }
        }
        if(null == optimalActivityRule) {
            //如果没有满足条件的取最小满足条件的一项
            optimalActivityRule = activityRuleList.get(activityRuleList.size()-1);
            optimalActivityRule.setReduceAmount(new BigDecimal("0"));
            optimalActivityRule.setSelectType(1);

            StringBuffer ruleDesc = new StringBuffer()
                    .append("满")
                    .append(optimalActivityRule.getConditionNum())
                    .append("元打")
                    .append(optimalActivityRule.getBenefitDiscount())
                    .append("折，还差")
                    .append(totalNum-optimalActivityRule.getConditionNum())
                    .append("件");
            optimalActivityRule.setRuleDesc(ruleDesc.toString());
        } else {
            StringBuffer ruleDesc = new StringBuffer()
                    .append("满")
                    .append(optimalActivityRule.getConditionNum())
                    .append("元打")
                    .append(optimalActivityRule.getBenefitDiscount())
                    .append("折，已减")
                    .append(optimalActivityRule.getReduceAmount())
                    .append("元");
            optimalActivityRule.setRuleDesc(ruleDesc.toString());
            optimalActivityRule.setSelectType(2);
        }
        return optimalActivityRule;
    }

    /**
     * 计算满减最优规则
     * @param totalAmount
     * @param activityRuleList //该活动规则skuActivityRuleList数据，已经按照优惠金额从大到小排序了
     */
    private ActivityRule computeFullReduction(BigDecimal totalAmount, List<ActivityRule> activityRuleList) {
        ActivityRule optimalActivityRule = null;
        //该活动规则skuActivityRuleList数据，已经按照优惠金额从大到小排序了
        for (ActivityRule activityRule : activityRuleList) {
            //如果订单项金额大于等于满减金额，则优惠金额
            if (totalAmount.compareTo(activityRule.getConditionAmount()) > -1) {
                //优惠后减少金额
                activityRule.setReduceAmount(activityRule.getBenefitAmount());
                optimalActivityRule = activityRule;
                break;
            }
        }
        if(null == optimalActivityRule) {
            //如果没有满足条件的取最小满足条件的一项
            optimalActivityRule = activityRuleList.get(activityRuleList.size()-1);
            optimalActivityRule.setReduceAmount(new BigDecimal("0"));
            optimalActivityRule.setSelectType(1);

            StringBuffer ruleDesc = new StringBuffer()
                    .append("满")
                    .append(optimalActivityRule.getConditionAmount())
                    .append("元减")
                    .append(optimalActivityRule.getBenefitAmount())
                    .append("元，还差")
                    .append(totalAmount.subtract(optimalActivityRule.getConditionAmount()))
                    .append("元");
            optimalActivityRule.setRuleDesc(ruleDesc.toString());
        } else {
            StringBuffer ruleDesc = new StringBuffer()
                    .append("满")
                    .append(optimalActivityRule.getConditionAmount())
                    .append("元减")
                    .append(optimalActivityRule.getBenefitAmount())
                    .append("元，已减")
                    .append(optimalActivityRule.getReduceAmount())
                    .append("元");
            optimalActivityRule.setRuleDesc(ruleDesc.toString());
            optimalActivityRule.setSelectType(2);
        }
        return optimalActivityRule;
    }

    private BigDecimal computeTotalAmount(List<CartInfo> cartInfoList) {
        BigDecimal total = new BigDecimal("0");
        for (CartInfo cartInfo : cartInfoList) {
            //是否选中
            if(cartInfo.getIsChecked().intValue() == 1) {
                BigDecimal itemTotal = cartInfo.getCartPrice().multiply(new BigDecimal(cartInfo.getSkuNum()));
                total = total.add(itemTotal);
            }
        }
        return total;
    }

    private int computeCartNum(List<CartInfo> cartInfoList) {
        int total = 0;
        for (CartInfo cartInfo : cartInfoList) {
            //是否选中
            if(cartInfo.getIsChecked().intValue() == 1) {
                total += cartInfo.getSkuNum();
            }
        }
        return total;
    }
}
