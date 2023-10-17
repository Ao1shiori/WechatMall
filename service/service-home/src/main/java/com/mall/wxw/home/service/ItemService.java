package com.mall.wxw.home.service;

import java.util.Map;

/**
 * @author: wxw24633
 * @Time: 2023/10/17  14:54
 */
public interface ItemService {

    Map<String, Object> item(Long id, Long userId);
}
