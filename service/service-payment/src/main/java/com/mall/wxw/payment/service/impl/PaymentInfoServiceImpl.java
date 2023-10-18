package com.mall.wxw.payment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.wxw.common.exception.MallException;
import com.mall.wxw.common.result.ResultCodeEnum;
import com.mall.wxw.enums.PaymentStatus;
import com.mall.wxw.enums.PaymentType;
import com.mall.wxw.model.order.OrderInfo;
import com.mall.wxw.model.order.PaymentInfo;
import com.mall.wxw.mq.constant.MqConst;
import com.mall.wxw.mq.service.RabbitService;
import com.mall.wxw.order.client.OrderFeignClient;
import com.mall.wxw.payment.mapper.PaymentInfoMapper;
import com.mall.wxw.payment.service.PaymentInfoService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

/**
 * @author: wxw24633
 * @Time: 2023/10/18  15:30
 */
@Service
public class PaymentInfoServiceImpl extends ServiceImpl<PaymentInfoMapper,PaymentInfo> implements PaymentInfoService {
    @Resource
    private OrderFeignClient orderFeignClient;
    @Resource
    private RabbitService rabbitService;

    @Override
    public PaymentInfo getPaymentInfoByOrderNo(String orderNo) {
        return baseMapper.selectOne(new LambdaQueryWrapper<PaymentInfo>().eq(PaymentInfo::getOrderId, orderNo));
    }

    @Override
    public PaymentInfo savePaymentInfo(String orderNo) {
        //rpc 根据orderNo查询订单信息
        OrderInfo order = orderFeignClient.getOrderInfo(orderNo);
        if (order==null){
            throw new MallException(ResultCodeEnum.DATA_ERROR);
        }
        // 保存交易记录
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setOrderId(order.getId());
        paymentInfo.setPaymentType(PaymentType.WEIXIN);
        paymentInfo.setUserId(order.getUserId());
        paymentInfo.setOrderNo(order.getOrderNo());
        paymentInfo.setPaymentStatus(PaymentStatus.UNPAID);
        String subject = "userID:" + order.getUserId() + "下单";
        paymentInfo.setSubject(subject);
        //paymentInfo.setTotalAmount(order.getTotalAmount());
        paymentInfo.setTotalAmount(new BigDecimal("0.01"));

        baseMapper.insert(paymentInfo);
        return paymentInfo;
    }

    //修改支付记录表 已经支付
    @Override
    public void paySuccess(String orderNo, Map<String, String> resultMap) {
        //查询订单支付记录表是否支付
        PaymentInfo paymentInfo = baseMapper.selectOne(new LambdaQueryWrapper<PaymentInfo>().eq(PaymentInfo::getOrderNo, orderNo));
        //已经支付
        if (paymentInfo.getPaymentStatus()!= PaymentStatus.UNPAID){
            return;
        }
        //没支付 更新 MQ修改订单记录 已经支付 库存扣减
        paymentInfo.setPaymentStatus(PaymentStatus.PAID);
        paymentInfo.setTradeNo(resultMap.get("transaction_id"));
        paymentInfo.setCallbackContent(resultMap.toString());
        baseMapper.updateById(paymentInfo);
        //mq修改订单记录 和扣减库存
        rabbitService.sendMessage(MqConst.EXCHANGE_PAY_DIRECT,MqConst.ROUTING_PAY_SUCCESS,orderNo);
    }
}
