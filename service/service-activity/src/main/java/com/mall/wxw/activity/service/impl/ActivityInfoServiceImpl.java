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
import com.mall.wxw.model.product.SkuInfo;
import com.mall.wxw.vo.activity.ActivityRuleVo;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
}
