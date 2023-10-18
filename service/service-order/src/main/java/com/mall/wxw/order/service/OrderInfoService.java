package com.mall.wxw.order.service;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.wxw.model.order.OrderInfo;
import com.mall.wxw.vo.order.OrderConfirmVo;
import com.mall.wxw.vo.order.OrderSubmitVo;
import com.mall.wxw.vo.order.OrderUserQueryVo;

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

    OrderInfo getOrderInfoByOrderNo(String orderNo);

    void orderPay(String orderNo);

    IPage<OrderInfo> findUserOrderPage(Page<OrderInfo> pageParam, OrderUserQueryVo orderUserQueryVo);
}
