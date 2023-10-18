package com.mall.wxw.order.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.wxw.cart.client.CartFeignClient;
import com.mall.wxw.client.activity.ActivityFeignClient;
import com.mall.wxw.client.user.UserFeignClient;
import com.mall.wxw.common.auth.AuthContextHolder;
import com.mall.wxw.common.constant.RedisConst;
import com.mall.wxw.common.exception.MallException;
import com.mall.wxw.common.result.ResultCodeEnum;
import com.mall.wxw.enums.SkuType;
import com.mall.wxw.model.order.CartInfo;
import com.mall.wxw.model.order.OrderInfo;
import com.mall.wxw.order.mapper.OrderInfoMapper;
import com.mall.wxw.order.service.OrderInfoService;
import com.mall.wxw.vo.order.OrderConfirmVo;
import com.mall.wxw.vo.order.OrderSubmitVo;
import com.mall.wxw.vo.product.SkuStockLockVo;
import com.mall.wxw.vo.user.LeaderAddressVo;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
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

    @Override
    public OrderConfirmVo confirmOrder() {
        //用户id
        Long userId = AuthContextHolder.getUserId();
        //用户对应团长信息
        LeaderAddressVo leaderAddressVo = userFeignClient.getUserAddressByUserId(userId);
        //获取购物车选中商品
        List<CartInfo> cartInfoList = cartFeignClient.getCartCheckedList(userId);
        // 防重：生成一个唯一标识，保存到redis中一份
        String orderNo = System.currentTimeMillis()+"";//IdWorker.getTimeId();
        redisTemplate.opsForValue().set(RedisConst.ORDER_REPEAT + orderNo, orderNo, 24, TimeUnit.HOURS);
        //获取购物车满足条件的促销与优惠券信息
        OrderConfirmVo orderConfirmVo = activityFeignClient.findCartActivityAndCoupon(cartInfoList, userId);
        //封装其他信息
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
        }

        //下单过程 向两张表加数据

        //返回订单id


        return null;
    }

    @Override
    public OrderInfo getOrderInfoById(Long orderId) {
        return null;
    }
}
