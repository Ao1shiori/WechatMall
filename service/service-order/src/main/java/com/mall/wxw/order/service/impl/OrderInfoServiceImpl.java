package com.mall.wxw.order.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.wxw.cart.client.CartFeignClient;
import com.mall.wxw.client.activity.ActivityFeignClient;
import com.mall.wxw.client.product.ProductFeignClient;
import com.mall.wxw.client.user.UserFeignClient;
import com.mall.wxw.common.auth.AuthContextHolder;
import com.mall.wxw.common.constant.RedisConst;
import com.mall.wxw.common.exception.MallException;
import com.mall.wxw.common.result.ResultCodeEnum;
import com.mall.wxw.common.utils.DateUtil;
import com.mall.wxw.enums.*;
import com.mall.wxw.model.activity.ActivityRule;
import com.mall.wxw.model.activity.CouponInfo;
import com.mall.wxw.model.order.CartInfo;
import com.mall.wxw.model.order.OrderInfo;
import com.mall.wxw.model.order.OrderItem;
import com.mall.wxw.mq.constant.MqConst;
import com.mall.wxw.mq.service.RabbitService;
import com.mall.wxw.order.mapper.OrderInfoMapper;
import com.mall.wxw.order.mapper.OrderItemMapper;
import com.mall.wxw.order.service.OrderInfoService;
import com.mall.wxw.vo.order.CartInfoVo;
import com.mall.wxw.vo.order.OrderConfirmVo;
import com.mall.wxw.vo.order.OrderSubmitVo;
import com.mall.wxw.vo.order.OrderUserQueryVo;
import com.mall.wxw.vo.product.SkuStockLockVo;
import com.mall.wxw.vo.user.LeaderAddressVo;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <p>
 * 订单 服务实现类
 * </p>
 *
 * @author wxw
 * @since 2023-10-18
 */
@Service
public class OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderInfoService {
    @Resource
    private UserFeignClient userFeignClient;
    @Resource
    private CartFeignClient cartFeignClient;
    @Resource
    private ActivityFeignClient activityFeignClient;
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private ProductFeignClient productFeignClient;
    @Resource
    private RabbitService rabbitService;
    @Resource
    private OrderItemMapper orderItemMapper;

    @Override
    public OrderConfirmVo confirmOrder() {
        // 获取当前用户的ID
        Long userId = AuthContextHolder.getUserId();
        // 调用远程服务获取用户对应的领导地址信息
        LeaderAddressVo leaderAddressVo = userFeignClient.getUserAddressByUserId(userId);
        // 调用远程服务获取购物车中被选中的商品列表
        List<CartInfo> cartInfoList = cartFeignClient.getCartCheckedList(userId);
        // 生成一个唯一标识，保存到Redis中一份，防止重复下单
        String orderNo = System.currentTimeMillis() + ""; // IdWorker.getTimeId();
        redisTemplate.opsForValue().set(RedisConst.ORDER_REPEAT + orderNo, orderNo, 24, TimeUnit.HOURS);
        // 调用远程服务获取购物车满足条件的促销与优惠券信息
        OrderConfirmVo orderConfirmVo = activityFeignClient.findCartActivityAndCoupon(cartInfoList, userId);
        // 将领导地址和订单号信息封装到OrderConfirmVo对象中
        orderConfirmVo.setLeaderAddressVo(leaderAddressVo);
        orderConfirmVo.setOrderNo(orderNo);
        return orderConfirmVo;
    }


    @Override
    public Long submitOrder(OrderSubmitVo orderParamVo) {
        //设置给哪个用户生成订单
        Long userId = orderParamVo.getUserId();
        orderParamVo.setUserId(userId);
        //订单重复提交验证 redis+lua保证原子性
        //获取唯一订单号 到redis查询
        String orderNo = orderParamVo.getOrderNo();
        if (StringUtils.isEmpty(orderNo)){
            //非法请求
            throw new MallException(ResultCodeEnum.ILLEGAL_REQUEST);
        }
        //lua保证原子性
        String script = "if(redis.call('get', KEYS[1]) == ARGV[1]) then return redis.call('del', KEYS[1]) else return 0 end";
        //如果有相同的则删除redis中数据
        Boolean flag = (Boolean) redisTemplate.execute(new DefaultRedisScript(script, Boolean.class),
                Arrays.asList(RedisConst.ORDER_REPEAT + orderNo), orderNo);
        //没有则表示重复提交 不能往后进行
        if (Boolean.FALSE.equals(flag)){
            //重复提交
            throw new MallException(ResultCodeEnum.REPEAT_SUBMIT);
        }
        //验证并锁定库存(没真正减)
        //rpc获取用户购物车选中的购物项
        List<CartInfo> cartInfoList = cartFeignClient.getCartCheckedList(userId);
        //根据商品类型处理
        List<CartInfo> commonSkuList = cartInfoList
                .stream().filter(cartInfo -> cartInfo.getSkuType() == SkuType.COMMON.getCode())
                .collect(Collectors.toList());
        //普通类型商品集合转换成List<SkuStockLockVo>
        if (!CollectionUtils.isEmpty(commonSkuList)){
            List<SkuStockLockVo> commonStockLockVoList = commonSkuList.stream().map(item -> {
                SkuStockLockVo skuStockLockVo = new SkuStockLockVo();
                skuStockLockVo.setSkuId(item.getSkuId());
                skuStockLockVo.setSkuNum(item.getSkuNum());
                return skuStockLockVo;
            }).collect(Collectors.toList());
            //rpc商品模块锁定商品
            //验证库存并锁定
            Boolean isLockSuccess = productFeignClient.checkAndLock(commonStockLockVoList, orderNo);
            if (!isLockSuccess){
                //锁定失败
                throw new MallException(ResultCodeEnum.ORDER_STOCK_FALL);
            }
        }
        //下单过程 向两张表加数据
        Long orderId = saveOrder(orderParamVo,cartInfoList);
        //下单完成mq删除购物车记录
        rabbitService.sendMessage(MqConst.EXCHANGE_ORDER_DIRECT,
                MqConst.ROUTING_DELETE_CART,
                userId);
        //返回订单id
        return orderId;
    }

    @Transactional(rollbackFor = Exception.class)
    public Long saveOrder(OrderSubmitVo orderParamVo, List<CartInfo> cartInfoList) {
        Long userId = AuthContextHolder.getUserId();
        if (CollectionUtils.isEmpty(cartInfoList)){
            throw new MallException(ResultCodeEnum.DATA_ERROR);
        }
        //查提货点和团长信息
        LeaderAddressVo leaderAddressVo = userFeignClient.getUserAddressByUserId(userId);
        if (leaderAddressVo == null){
            throw new MallException(ResultCodeEnum.DATA_ERROR);
        }
        //计算金额
        //营销活动和优惠券金额
        Map<String, BigDecimal> activitySplitAmount = computeActivitySplitAmount(cartInfoList);
        Map<String, BigDecimal> couponInfoSplitAmount = computeCouponInfoSplitAmount(cartInfoList, orderParamVo.getCouponId());
        //sku对应的订单明细
        List<OrderItem> orderItemList = new ArrayList<>();
        for (CartInfo cartInfo : cartInfoList) {
            OrderItem orderItem = new OrderItem();
            orderItem.setId(null);
            orderItem.setCategoryId(cartInfo.getCategoryId());
            if(cartInfo.getSkuType() == SkuType.COMMON.getCode()) {
                orderItem.setSkuType(SkuType.COMMON);
            } else {
                orderItem.setSkuType(SkuType.SECKILL);
            }
            orderItem.setSkuId(cartInfo.getSkuId());
            orderItem.setSkuName(cartInfo.getSkuName());
            orderItem.setSkuPrice(cartInfo.getCartPrice());
            orderItem.setImgUrl(cartInfo.getImgUrl());
            orderItem.setSkuNum(cartInfo.getSkuNum());
            orderItem.setLeaderId(orderParamVo.getLeaderId());
            //促销活动分摊金额
            BigDecimal splitActivityAmount = activitySplitAmount.get("activity:"+orderItem.getSkuId());
            if(null == splitActivityAmount) {
                splitActivityAmount = new BigDecimal(0);
            }
            orderItem.setSplitActivityAmount(splitActivityAmount);
            //优惠券分摊金额
            BigDecimal splitCouponAmount = couponInfoSplitAmount.get("coupon:"+orderItem.getSkuId());
            if(null == splitCouponAmount) {
                splitCouponAmount = new BigDecimal(0);
            }
            orderItem.setSplitCouponAmount(splitCouponAmount);
            //总金额
            BigDecimal skuTotalAmount = orderItem.getSkuPrice().multiply(new BigDecimal(orderItem.getSkuNum()));
            //优惠后总金额
            BigDecimal splitTotalAmount = skuTotalAmount.subtract(splitActivityAmount).subtract(splitCouponAmount);
            orderItem.setSplitTotalAmount(splitTotalAmount);
            orderItemList.add(orderItem);
        }
        //封装OrderInfo
        OrderInfo order = new OrderInfo();
        order.setUserId(userId);
		//private String nickName;
        order.setOrderNo(orderParamVo.getOrderNo());
        order.setOrderStatus(OrderStatus.UNPAID);
        order.setProcessStatus(ProcessStatus.UNPAID);
        order.setCouponId(orderParamVo.getCouponId());
        order.setLeaderId(orderParamVo.getLeaderId());
        order.setLeaderName(leaderAddressVo.getLeaderName());
        order.setLeaderPhone(leaderAddressVo.getLeaderPhone());
        order.setTakeName(leaderAddressVo.getTakeName());
        order.setReceiverName(orderParamVo.getReceiverName());
        order.setReceiverPhone(orderParamVo.getReceiverPhone());
        order.setReceiverProvince(leaderAddressVo.getProvince());
        order.setReceiverCity(leaderAddressVo.getCity());
        order.setReceiverDistrict(leaderAddressVo.getDistrict());
        order.setReceiverAddress(leaderAddressVo.getDetailAddress());
        order.setWareId(cartInfoList.get(0).getWareId());
        //计算订单金额
        BigDecimal originalTotalAmount = this.computeTotalAmount(cartInfoList);
        BigDecimal activityAmount = activitySplitAmount.get("activity:total");
        if(null == activityAmount) activityAmount = new BigDecimal(0);
        BigDecimal couponAmount = couponInfoSplitAmount.get("coupon:total");
        if(null == couponAmount) couponAmount = new BigDecimal(0);
        BigDecimal totalAmount = originalTotalAmount.subtract(activityAmount).subtract(couponAmount);
        //计算订单金额
        order.setOriginalTotalAmount(originalTotalAmount);
        order.setActivityAmount(activityAmount);
        order.setCouponAmount(couponAmount);
        order.setTotalAmount(totalAmount);
        //计算团长佣金
        BigDecimal profitRate = new BigDecimal(0);
        BigDecimal commissionAmount = order.getTotalAmount().multiply(profitRate);
        order.setCommissionAmount(commissionAmount);
        //保存订单基本信息数据
        baseMapper.insert(order);
        //保存订单项数据
        orderItemList.forEach(orderItem -> {
            orderItem.setOrderId(order.getId());
            orderItemMapper.insert(orderItem);
        });
        //更新优惠券使用状态
        if(null != order.getCouponId()) {
            activityFeignClient.updateCouponInfoUseStatus(order.getCouponId(), userId, order.getId());
        }
        //下单成功记录用户购物数量 redis
        String orderSkuKey = RedisConst.ORDER_SKU_MAP + orderParamVo.getUserId();
        BoundHashOperations<String, String, Integer> hashOperations = redisTemplate.boundHashOps(orderSkuKey);
        cartInfoList.forEach(cartInfo -> {
            if(hashOperations.hasKey(cartInfo.getSkuId().toString())) {
                Integer orderSkuNum = hashOperations.get(cartInfo.getSkuId().toString()) + cartInfo.getSkuNum();
                hashOperations.put(cartInfo.getSkuId().toString(), orderSkuNum);
            }
        });
        redisTemplate.expire(orderSkuKey, DateUtil.getCurrentExpireTimes(), TimeUnit.SECONDS);
        //返回订单id
        return order.getId();
    }

    //订单详情
    @Override
    public OrderInfo getOrderInfoById(Long orderId) {
        // 根据订单ID查询订单信息
        OrderInfo orderInfo = baseMapper.selectById(orderId);
        // 将订单状态名称添加到订单参数中
        orderInfo.getParam().put("orderStatusName", orderInfo.getOrderStatus().getComment());
        // 根据订单ID查询订单商品列表
        List<OrderItem> orderItemList = orderItemMapper
                .selectList(new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderInfo.getId()));
        // 将订单商品列表添加到订单信息中
        orderInfo.setOrderItemList(orderItemList);
        return orderInfo;
    }

    @Override
    public OrderInfo getOrderInfoByOrderNo(String orderNo) {
        // 使用LambdaQueryWrapper来构建查询条件
        return baseMapper.selectOne(new LambdaQueryWrapper<OrderInfo>().eq(OrderInfo::getOrderNo, orderNo));
    }


    //更新订单状态 扣减库存
    @Override
    public void orderPay(String orderNo) {
        // 根据订单号查询订单信息
        OrderInfo orderInfo = getOrderInfoByOrderNo(orderNo);
        // 如果订单不存在或者订单状态不是未支付，则直接返回
        if (orderInfo == null || orderInfo.getOrderStatus()!= OrderStatus.UNPAID){
            return;
        }
        // 更新订单状态
        updateOrderStatus(orderInfo.getId());
        // 发送消息到消息队列，减少库存
        rabbitService.sendMessage(MqConst.EXCHANGE_ORDER_DIRECT, MqConst.ROUTING_MINUS_STOCK, orderNo);
    }


    @Override
    public IPage<OrderInfo> findUserOrderPage(Page<OrderInfo> pageParam, OrderUserQueryVo orderUserQueryVo) {
        LambdaQueryWrapper<OrderInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderInfo::getUserId,orderUserQueryVo.getUserId());
        wrapper.eq(OrderInfo::getOrderStatus,orderUserQueryVo.getOrderStatus());
        IPage<OrderInfo> pageModel = baseMapper.selectPage(pageParam, wrapper);
        //获取每个订单，把每个订单里面订单项查询封装
        List<OrderInfo> orderInfoList = pageModel.getRecords();
        for(OrderInfo orderInfo : orderInfoList) {
            //根据订单id查询里面所有订单项列表
            List<OrderItem> orderItemList = orderItemMapper.selectList(
                    new LambdaQueryWrapper<OrderItem>()
                            .eq(OrderItem::getOrderId, orderInfo.getId())
            );
            //把订单项集合封装到每个订单里面
            orderInfo.setOrderItemList(orderItemList);
            //封装订单状态名称
            orderInfo.getParam().put("orderStatusName",orderInfo.getOrderStatus().getComment());
        }
        return pageModel;
    }

    private void updateOrderStatus(Long orderId) {
        // 根据订单ID查询订单信息
        OrderInfo orderInfo = baseMapper.selectById(orderId);
        // 将订单状态设置为等待发货
        orderInfo.setOrderStatus(OrderStatus.WAITING_DELEVER);
        // 将处理状态设置为等待发货
        orderInfo.setProcessStatus(ProcessStatus.WAITING_DELEVER);
        // 更新订单信息
        baseMapper.updateById(orderInfo);
    }

    //计算总金额
    private BigDecimal computeTotalAmount(List<CartInfo> cartInfoList) {
        BigDecimal total = new BigDecimal(0);
        for (CartInfo cartInfo : cartInfoList) {
            BigDecimal itemTotal = cartInfo.getCartPrice().multiply(new BigDecimal(cartInfo.getSkuNum()));
            total = total.add(itemTotal);
        }
        return total;
    }

    /**
     * 计算购物项分摊的优惠减少金额
     * 打折：按折扣分担
     * 现金：按比例分摊
     * @param cartInfoParamList
     * @return
     */
    private Map<String, BigDecimal> computeActivitySplitAmount(List<CartInfo> cartInfoParamList) {
        Map<String, BigDecimal> activitySplitAmountMap = new HashMap<>();

        //促销活动相关信息
        List<CartInfoVo> cartInfoVoList = activityFeignClient.findCartActivityList(cartInfoParamList);

        //活动总金额
        BigDecimal activityReduceAmount = new BigDecimal(0);
        if(!CollectionUtils.isEmpty(cartInfoVoList)) {
            for(CartInfoVo cartInfoVo : cartInfoVoList) {
                ActivityRule activityRule = cartInfoVo.getActivityRule();
                List<CartInfo> cartInfoList = cartInfoVo.getCartInfoList();
                if(null != activityRule) {
                    //优惠金额， 按比例分摊
                    BigDecimal reduceAmount = activityRule.getReduceAmount();
                    activityReduceAmount = activityReduceAmount.add(reduceAmount);
                    if(cartInfoList.size() == 1) {
                        activitySplitAmountMap.put("activity:"+cartInfoList.get(0).getSkuId(), reduceAmount);
                    } else {
                        //总金额
                        BigDecimal originalTotalAmount = new BigDecimal(0);
                        for(CartInfo cartInfo : cartInfoList) {
                            BigDecimal skuTotalAmount = cartInfo.getCartPrice().multiply(new BigDecimal(cartInfo.getSkuNum()));
                            originalTotalAmount = originalTotalAmount.add(skuTotalAmount);
                        }
                        //记录除最后一项是所有分摊金额， 最后一项=总的 - skuPartReduceAmount
                        BigDecimal skuPartReduceAmount = new BigDecimal(0);
                        if (activityRule.getActivityType() == ActivityType.FULL_REDUCTION) {
                            for(int i=0, len=cartInfoList.size(); i<len; i++) {
                                CartInfo cartInfo = cartInfoList.get(i);
                                if(i < len -1) {
                                    BigDecimal skuTotalAmount = cartInfo.getCartPrice().multiply(new BigDecimal(cartInfo.getSkuNum()));
                                    //sku分摊金额
                                    BigDecimal skuReduceAmount = skuTotalAmount.divide(originalTotalAmount, 2, RoundingMode.HALF_UP).multiply(reduceAmount);
                                    activitySplitAmountMap.put("activity:"+cartInfo.getSkuId(), skuReduceAmount);

                                    skuPartReduceAmount = skuPartReduceAmount.add(skuReduceAmount);
                                } else {
                                    BigDecimal skuReduceAmount = reduceAmount.subtract(skuPartReduceAmount);
                                    activitySplitAmountMap.put("activity:"+cartInfo.getSkuId(), skuReduceAmount);
                                }
                            }
                        } else {
                            for(int i=0, len=cartInfoList.size(); i<len; i++) {
                                CartInfo cartInfo = cartInfoList.get(i);
                                if(i < len -1) {
                                    BigDecimal skuTotalAmount = cartInfo.getCartPrice().multiply(new BigDecimal(cartInfo.getSkuNum()));

                                    //sku分摊金额
                                    BigDecimal skuDiscountTotalAmount = skuTotalAmount.multiply(activityRule.getBenefitDiscount().divide(new BigDecimal("10")));
                                    BigDecimal skuReduceAmount = skuTotalAmount.subtract(skuDiscountTotalAmount);
                                    activitySplitAmountMap.put("activity:"+cartInfo.getSkuId(), skuReduceAmount);

                                    skuPartReduceAmount = skuPartReduceAmount.add(skuReduceAmount);
                                } else {
                                    BigDecimal skuReduceAmount = reduceAmount.subtract(skuPartReduceAmount);
                                    activitySplitAmountMap.put("activity:"+cartInfo.getSkuId(), skuReduceAmount);
                                }
                            }
                        }
                    }
                }
            }
        }
        activitySplitAmountMap.put("activity:total", activityReduceAmount);
        return activitySplitAmountMap;
    }

    //优惠卷优惠金额
    private Map<String, BigDecimal> computeCouponInfoSplitAmount(List<CartInfo> cartInfoList, Long couponId) {
        Map<String, BigDecimal> couponInfoSplitAmountMap = new HashMap<>();

        if(null == couponId) return couponInfoSplitAmountMap;
        CouponInfo couponInfo = activityFeignClient.findRangeSkuIdList(cartInfoList, couponId);

        if(null != couponInfo) {
            //sku对应的订单明细
            Map<Long, CartInfo> skuIdToCartInfoMap = new HashMap<>();
            for (CartInfo cartInfo : cartInfoList) {
                skuIdToCartInfoMap.put(cartInfo.getSkuId(), cartInfo);
            }
            //优惠券对应的skuId列表
            List<Long> skuIdList = couponInfo.getSkuIdList();
            if(CollectionUtils.isEmpty(skuIdList)) {
                return couponInfoSplitAmountMap;
            }
            //优惠券优化总金额
            BigDecimal reduceAmount = couponInfo.getAmount();
            if(skuIdList.size() == 1) {
                //sku的优化金额
                couponInfoSplitAmountMap.put("coupon:"+skuIdToCartInfoMap.get(skuIdList.get(0)).getSkuId(), reduceAmount);
            } else {
                //总金额
                BigDecimal originalTotalAmount = new BigDecimal(0);
                for (Long skuId : skuIdList) {
                    CartInfo cartInfo = skuIdToCartInfoMap.get(skuId);
                    BigDecimal skuTotalAmount = cartInfo.getCartPrice().multiply(new BigDecimal(cartInfo.getSkuNum()));
                    originalTotalAmount = originalTotalAmount.add(skuTotalAmount);
                }
                //记录除最后一项是所有分摊金额， 最后一项=总的 - skuPartReduceAmount
                BigDecimal skuPartReduceAmount = new BigDecimal(0);
                if (couponInfo.getCouponType() == CouponType.CASH || couponInfo.getCouponType() == CouponType.FULL_REDUCTION) {
                    for(int i=0, len=skuIdList.size(); i<len; i++) {
                        CartInfo cartInfo = skuIdToCartInfoMap.get(skuIdList.get(i));
                        if(i < len -1) {
                            BigDecimal skuTotalAmount = cartInfo.getCartPrice().multiply(new BigDecimal(cartInfo.getSkuNum()));
                            //sku分摊金额
                            BigDecimal skuReduceAmount = skuTotalAmount.divide(originalTotalAmount, 2, RoundingMode.HALF_UP).multiply(reduceAmount);
                            couponInfoSplitAmountMap.put("coupon:"+cartInfo.getSkuId(), skuReduceAmount);

                            skuPartReduceAmount = skuPartReduceAmount.add(skuReduceAmount);
                        } else {
                            BigDecimal skuReduceAmount = reduceAmount.subtract(skuPartReduceAmount);
                            couponInfoSplitAmountMap.put("coupon:"+cartInfo.getSkuId(), skuReduceAmount);
                        }
                    }
                }
            }
            couponInfoSplitAmountMap.put("coupon:total", couponInfo.getAmount());
        }
        return couponInfoSplitAmountMap;
    }
}
