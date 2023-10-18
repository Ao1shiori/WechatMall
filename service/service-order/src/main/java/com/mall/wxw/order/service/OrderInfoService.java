package com.mall.wxw.order.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.wxw.model.order.OrderInfo;
import com.mall.wxw.vo.order.OrderConfirmVo;
import com.mall.wxw.vo.order.OrderSubmitVo;

/**
 * <p>
 * 订单 服务类
 * </p>
 *
 * @author wxw
 * @since 2023-10-18
 */
public interface OrderInfoService extends IService<OrderInfo> {

    OrderConfirmVo confirmOrder();

    Long submitOrder(OrderSubmitVo orderParamVo);

    OrderInfo getOrderInfoById(Long orderId);
}
