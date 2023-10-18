package com.mall.wxw.payment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.wxw.model.order.PaymentInfo;

import java.util.Map;

/**
 * @author: wxw24633
 * @Time: 2023/10/18  15:29
 */
public interface PaymentInfoService extends IService<PaymentInfo> {
    PaymentInfo getPaymentInfoByOrderNo(String orderNo);

    PaymentInfo savePaymentInfo(String orderNo);

    void paySuccess(String out_trade_no, Map<String, String> resultMap);
}
