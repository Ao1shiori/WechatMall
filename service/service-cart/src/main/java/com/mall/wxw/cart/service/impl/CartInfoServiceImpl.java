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
import java.util.stream.Collectors;

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
        // 获取 Redis 中购物车数据的哈希操作对象
        BoundHashOperations<String, String, CartInfo> hashOperations = redisTemplate.boundHashOps(getCartKey(userId));
        // 遍历需要删除的 SKU ID 列表
        skuIdList.forEach(skuId -> {
            // 从购物车中删除指定 SKU
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
        // 获取购物车的 Redis Key
        String cartKey = getCartKey(userId);

        // 获取 Redis 中购物车数据的哈希操作对象
        BoundHashOperations<String, String, CartInfo> boundHashOps = redisTemplate.boundHashOps(cartKey);

        // 从购物车中获取指定 SKU 的信息
        CartInfo cartInfo = boundHashOps.get(skuId.toString());

        // 如果购物车中存在该 SKU 的信息
        if (null != cartInfo) {
            // 设置该 SKU 是否被选中
            cartInfo.setIsChecked(isChecked);

            // 更新购物车中的信息
            boundHashOps.put(skuId.toString(), cartInfo);

            // 更新购物车 Key 的过期时间
            this.setCartKeyExpire(cartKey);
        }
    }


    @Override
    public void checkAllCart(Long userId, Integer isChecked) {
        // 获取购物车的 Redis Key
        String cartKey = this.getCartKey(userId);

        // 获取 Redis 中购物车数据的哈希操作对象
        BoundHashOperations<String, String, CartInfo> boundHashOps = this.redisTemplate.boundHashOps(cartKey);

        // 遍历购物车中所有的商品信息
        boundHashOps.values().forEach(cartInfo -> {
            // 设置商品是否被选中
            cartInfo.setIsChecked(isChecked);

            // 更新购物车中的商品信息
            boundHashOps.put(cartInfo.getSkuId().toString(), cartInfo);
        });

        // 更新购物车 Key 的过期时间
        this.setCartKeyExpire(cartKey);
    }

    @Override
    public void batchCheckCart(List<Long> skuIdList, Long userId, Integer isChecked) {
        // 获取购物车的 Redis Key
        String cartKey = getCartKey(userId);

        // 获取 Redis 中购物车数据的哈希操作对象
        BoundHashOperations<String, String, CartInfo> hashOperations = redisTemplate.boundHashOps(cartKey);
        // 遍历需要批量操作的 SKU ID 列表
        skuIdList.forEach(skuId -> {
            // 从购物车中获取指定 SKU 的信息
            CartInfo cartInfo = hashOperations.get(skuId.toString());

            // 设置商品是否被选中
            cartInfo.setIsChecked(isChecked);

            // 更新购物车中的商品信息
            hashOperations.put(cartInfo.getSkuId().toString(), cartInfo);
        });
    }


    @Override
    public List<CartInfo> getCartCheckedList(Long userId) {
        // 获取购物车的 Redis Key
        String cartKey = this.getCartKey(userId);

        // 获取 Redis 中购物车数据的哈希操作对象
        BoundHashOperations<String, String, CartInfo> boundHashOps = this.redisTemplate.boundHashOps(cartKey);

        // 获取购物车中已选中的商品列表
        List<CartInfo> cartInfoCheckList = boundHashOps.values()
                .stream()
                .filter((cartInfo) -> cartInfo.getIsChecked() == 1).collect(Collectors.toList());

        // 返回已选中的购物车商品列表
        return cartInfoCheckList;
    }


    //删除选用的购物车记录
    @Override
    public void deleteCartChecked(Long userId) {
        //根据userid查询选中购物车记录
        List<CartInfo> cartCheckedList = getCartCheckedList(userId);
        //根据记录得到skuid集合
        List<Long> skuIdList = cartCheckedList.stream().map(CartInfo::getSkuId).collect(Collectors.toList());
        //构建redis的key
        String cartKey = getCartKey(userId);
        //根据key查询field value
        BoundHashOperations<String,String,CartInfo> hashOperations = redisTemplate.boundHashOps(cartKey);
        //根据key删除redis数据
        skuIdList.forEach(skuId->{
            hashOperations.delete(skuId.toString());
        });
    }
}
