package com.mall.wxw.payment.service;

import java.util.Map;

public interface WeixinService {

	Map<String, String> createJsapi(String orderNo);


	Map<String, String> queryPayStatus(String orderNo);
}