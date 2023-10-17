package com.mall.wxw.cart.service.impl;

import com.mall.wxw.cart.service.CartInfoService;
import com.mall.wxw.client.product.ProductFeignClient;
import com.mall.wxw.common.constant.RedisConst;
import com.mall.wxw.common.exception.MallException;
import com.mall.wxw.common.result.ResultCodeEnum;
import com.mall.wxw.enums.SkuType;
import com.mall.wxw.model.order.CartInfo;
import com.mall.wxw.model.product.SkuInfo;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author: wxw24633
 * @Time: 2023/10/17  16:19
 */
@Service
public class CartInfoServiceImpl implements CartInfoService {
    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private ProductFeignClient productFeignClient;

    //返回购物车在redis的key
    private String getCartKey(Long userId){
        return RedisConst.USER_KEY_PREFIX + userId + RedisConst.USER_CART_KEY_SUFFIX;
    }

    //设置有效时间
    private void setCartKeyExpire(String key){
        redisTemplate.expire(key,RedisConst.USER_CART_EXPIRE, TimeUnit.SECONDS);
    }

    @Override
    public void addToCart(Long userId, Long skuId, Integer skuNum) {
        //根据key从redis获取数据
        String cartKey = getCartKey(userId);
        BoundHashOperations<String,String,CartInfo> hashOperations = redisTemplate.boundHashOps(cartKey);
        //是否第一次添加 判断结果是否有skuId 得到的为skuid+skuNum
        CartInfo cartInfo;
        if (hashOperations.hasKey(skuId.toString())){
            //不是第一次 更新skuNum
            cartInfo = hashOperations.get(skuId.toString());
            Integer currentNum = cartInfo.getSkuNum() + skuNum;
            if (currentNum < 1){
                return;
            }
            cartInfo.setCurrentBuyNum(currentNum);
            cartInfo.setSkuNum(currentNum);
            //不能大于限购数量
            Integer perLimit = cartInfo.getPerLimit();
            if (perLimit < currentNum){
                throw new MallException(ResultCodeEnum.SKU_LIMIT_ERROR);
            }
            cartInfo.setIsChecked(1);
            cartInfo.setUpdateTime(new Date());
        }else {
            //没有skuid 添加
            skuNum = 1;
            cartInfo = new CartInfo();
            SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
            if (skuInfo == null){
                throw new MallException(ResultCodeEnum.DATA_ERROR);
            }
            //封装数据
            cartInfo.setSkuId(skuId);
            cartInfo.setCategoryId(skuInfo.getCategoryId());
            cartInfo.setSkuType(skuInfo.getSkuType());
            cartInfo.setIsNewPerson(skuInfo.getIsNewPerson());
            cartInfo.setUserId(userId);
            cartInfo.setCartPrice(skuInfo.getPrice());
            cartInfo.setSkuNum(skuNum);
            cartInfo.setCurrentBuyNum(skuNum);
            cartInfo.setSkuType(SkuType.COMMON.getCode());
            cartInfo.setPerLimit(skuInfo.getPerLimit());
            cartInfo.setImgUrl(skuInfo.getImgUrl());
            cartInfo.setSkuName(skuInfo.getSkuName());
            cartInfo.setWareId(skuInfo.getWareId());
            cartInfo.setIsChecked(1);
            cartInfo.setStatus(1);
            cartInfo.setCreateTime(new Date());
            cartInfo.setUpdateTime(new Date());
        }
        //更新redis
        hashOperations.put(skuId.toString(),cartInfo);
        //设置有效时间
        setCartKeyExpire(cartKey);
    }

    @Override
    public void deleteCart(Long skuId, Long userId) {
        BoundHashOperations<String,String,CartInfo> hashOperations = redisTemplate.boundHashOps(getCartKey(userId));
        if (hashOperations.hasKey(skuId.toString())){
            hashOperations.delete(skuId.toString());
        }
    }

    @Override
    public void deleteAllCart(Long userId) {
        BoundHashOperations<String,String,CartInfo> hashOperations = redisTemplate.boundHashOps(getCartKey(userId));
        for (CartInfo cartInfo : hashOperations.values()) {
            hashOperations.delete(cartInfo.getSkuId().toString());
        }

    }

    @Override
    public void batchDeleteCart(List<Long> skuIdList, Long userId) {
        BoundHashOperations<String,String,CartInfo> hashOperations = redisTemplate.boundHashOps(getCartKey(userId));
        skuIdList.forEach(skuId->{
            hashOperations.delete(skuId.toString());
        });
    }

    //购物车列表
    @Override
    public List<CartInfo> getCartList(Long userId) {
        List<CartInfo> cartInfoList = new ArrayList<>();
        if (StringUtils.isEmpty(userId.toString())){
            return cartInfoList;
        }
        String cartKey = getCartKey(userId);
        BoundHashOperations<String,String,CartInfo> hashOperations = redisTemplate.boundHashOps(cartKey);
        cartInfoList = hashOperations.values();
        if (!CollectionUtils.isEmpty(cartInfoList)){
            //根据添加时间降序排序
            cartInfoList.sort(new Comparator<CartInfo>() {
                @Override
                public int compare(CartInfo o1, CartInfo o2) {
                    return o1.getCreateTime().compareTo(o2.getCreateTime());
                }
            });
        }
        return cartInfoList;
    }

    @Override
    public void checkCart(Long userId, Integer isChecked, Long skuId) {
        String cartKey = this.getCartKey(userId);
        BoundHashOperations<String, String, CartInfo> boundHashOps = this.redisTemplate.boundHashOps(cartKey);
        CartInfo cartInfo = boundHashOps.get(skuId.toString());
        if(null != cartInfo) {
            cartInfo.setIsChecked(isChecked);
            boundHashOps.put(skuId.toString(), cartInfo);
            this.setCartKeyExpire(cartKey);
        }
    }

    @Override
    public void checkAllCart(Long userId, Integer isChecked) {
        String cartKey = this.getCartKey(userId);
        BoundHashOperations<String, String, CartInfo> boundHashOps = this.redisTemplate.boundHashOps(cartKey);
        boundHashOps.values().forEach(cartInfo -> {
            cartInfo.setIsChecked(isChecked);
            boundHashOps.put(cartInfo.getSkuId().toString(), cartInfo);
        });
        this.setCartKeyExpire(cartKey);
    }

    @Override
    public void batchCheckCart(List<Long> skuIdList, Long userId, Integer isChecked) {
        String cartKey = getCartKey(userId);
        //获取缓存对象
        BoundHashOperations<String, String, CartInfo> hashOperations = redisTemplate.boundHashOps(cartKey);
        skuIdList.forEach(skuId -> {
            CartInfo cartInfo = hashOperations.get(skuId.toString());
            cartInfo.setIsChecked(isChecked);
            hashOperations.put(cartInfo.getSkuId().toString(), cartInfo);
        });
    }
}
