package com.mall.wxw.payment.controller;

import com.mall.wxw.common.result.Result;
import com.mall.wxw.common.result.ResultCodeEnum;
import com.mall.wxw.payment.service.PaymentInfoService;
import com.mall.wxw.payment.service.WeixinService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

/**
 * <p>
 * 微信支付 API
 * </p>
 */
@Api(tags = "微信支付接口")
@RestController
@RequestMapping("/api/payment/weixin")
@Slf4j
public class WeixinController {
    @Resource
    private WeixinService weixinPayService;

    @Resource
    private PaymentInfoService paymentInfoService;

    @ApiOperation(value = "下单 小程序支付")
    @GetMapping("/createJsapi/{orderNo}")
    public Result createJsapi(
            @ApiParam(name = "orderNo", value = "订单No", required = true)
            @PathVariable("orderNo") String orderNo) {
        Map<String,String> map = weixinPayService.createJsapi(orderNo);
        return Result.ok(map);
    }

    @ApiOperation(value = "查询支付状态")
    @GetMapping("/queryPayStatus/{orderNo}")
    public Result queryPayStatus(
            @ApiParam(name = "orderNo", value = "订单No", required = true)
            @PathVariable("orderNo") String orderNo) {
        //调用查询接口
        Map<String, String> resultMap = weixinPayService.queryPayStatus(orderNo);
        if (resultMap == null) {//出错
            return Result.build(ResultCodeEnum.PAYMENT_FAIL.getCode(),ResultCodeEnum.PAYMENT_FAIL.getMessage(),null);
        }
        if ("SUCCESS".equals(resultMap.get("trade_state"))) {//如果成功
            //更改订单状态，处理支付结果
            String out_trade_no = resultMap.get("out_trade_no");
            paymentInfoService.paySuccess(out_trade_no, resultMap);
            return Result.build(ResultCodeEnum.PAYMENT_SUCCESS.getCode(),ResultCodeEnum.PAYMENT_SUCCESS.getMessage(),null);
        }
        return Result.build(ResultCodeEnum.PAYMENT_ING.getCode(),ResultCodeEnum.PAYMENT_ING.getMessage(),null);
    }


}