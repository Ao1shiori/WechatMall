package com.mall.wxw.product.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.wxw.common.constant.RedisConst;
import com.mall.wxw.common.exception.MallException;
import com.mall.wxw.common.result.ResultCodeEnum;
import com.mall.wxw.model.product.SkuAttrValue;
import com.mall.wxw.model.product.SkuImage;
import com.mall.wxw.model.product.SkuInfo;
import com.mall.wxw.model.product.SkuPoster;
import com.mall.wxw.mq.constant.MqConst;
import com.mall.wxw.mq.service.RabbitService;
import com.mall.wxw.product.mapper.SkuInfoMapper;
import com.mall.wxw.product.service.SkuAttrValueService;
import com.mall.wxw.product.service.SkuImageService;
import com.mall.wxw.product.service.SkuInfoService;
import com.mall.wxw.product.service.SkuPosterService;
import com.mall.wxw.vo.product.SkuInfoQueryVo;
import com.mall.wxw.vo.product.SkuInfoVo;
import com.mall.wxw.vo.product.SkuStockLockVo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * sku信息 服务实现类
 * </p>
 *
 * @author wxw
 * @since 2023-10-15
 */
@Service
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoMapper, SkuInfo> implements SkuInfoService {
    @Resource
    private SkuPosterService skuPosterService;

    @Resource
    private SkuImageService skuImagesService;

    @Resource
    private SkuAttrValueService skuAttrValueService;

    @Resource
    private SkuInfoMapper skuInfoMapper;

    @Resource
    private RabbitService rabbitService;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private RedissonClient redissonClient;

    //获取sku分页列表
    @Override
    public IPage<SkuInfo> selectPage(Page<SkuInfo> pageParam, SkuInfoQueryVo skuInfoQueryVo) {
        // 获取查询关键字
        String keyword = skuInfoQueryVo.getKeyword();
        // 获取 SKU 类型
        String skuType = skuInfoQueryVo.getSkuType();
        // 获取商品分类 ID
        Long categoryId = skuInfoQueryVo.getCategoryId();
        // 构建查询条件
        LambdaQueryWrapper<SkuInfo> wrapper = new LambdaQueryWrapper<>();
        if (!StringUtils.isEmpty(keyword)) {
            wrapper.like(SkuInfo::getSkuName, keyword);
        }
        if (!StringUtils.isEmpty(skuType)) {
            wrapper.eq(SkuInfo::getSkuType, skuType);
        }
        if (!StringUtils.isEmpty(categoryId)) {
            wrapper.eq(SkuInfo::getCategoryId, categoryId);
        }
        // 调用查询方法返回结果
        return baseMapper.selectPage(pageParam, wrapper);
    }


    //添加商品
    @Transactional(rollbackFor = {Exception.class})
    @Override
    public void saveSkuInfo(SkuInfoVo skuInfoVo) {
        //保存sku信息
        SkuInfo skuInfo = new SkuInfo();
        BeanUtils.copyProperties(skuInfoVo, skuInfo);
        this.save(skuInfo);

        //保存sku海报
        List<SkuPoster> skuPosterList = skuInfoVo.getSkuPosterList();
        if(!CollectionUtils.isEmpty(skuPosterList)) {
            for(SkuPoster skuPoster : skuPosterList) {
                skuPoster.setSkuId(skuInfo.getId());
            }
            skuPosterService.saveBatch(skuPosterList);
        }

        //保存sku图片
        List<SkuImage> skuImagesList = skuInfoVo.getSkuImagesList();
        if(!CollectionUtils.isEmpty(skuImagesList)) {
            int sort = 1;
            for(SkuImage skuImages : skuImagesList) {
                skuImages.setSkuId(skuInfo.getId());
                skuImages.setSort(sort);
                sort++;
            }
            skuImagesService.saveBatch(skuImagesList);
        }

        //保存sku平台属性
        List<SkuAttrValue> skuAttrValueList = skuInfoVo.getSkuAttrValueList();
        if(!CollectionUtils.isEmpty(skuAttrValueList)) {
            int sort = 1;
            for(SkuAttrValue skuAttrValue : skuAttrValueList) {
                skuAttrValue.setSkuId(skuInfo.getId());
                skuAttrValue.setSort(sort);
                sort++;
            }
            skuAttrValueService.saveBatch(skuAttrValueList);
        }
    }

    @Override
    public SkuInfoVo getSkuInfoVo(Long id) {
        // 创建一个用于存储 SKU 详细信息的对象
        SkuInfoVo skuInfoVo = new SkuInfoVo();

        // 通过商品 ID 查询 SKU 的基本信息
        SkuInfo skuInfo = baseMapper.selectById(id);

        // 通过商品 ID 查询该 SKU 的商品图片列表
        List<SkuImage> skuImageList = skuImagesService.getImageListBySkuId(id);

        // 通过商品 ID 查询该 SKU 的商品海报列表
        List<SkuPoster> skuPosterList = skuPosterService.getPosterListBySkuId(id);

        // 通过商品 ID 查询该 SKU 的属性信息列表
        List<SkuAttrValue> skuAttrValueList = skuAttrValueService.getAttrValueListBySkuId(id);

        // 将以上查询的信息封装到一个完整的数据对象中，用于返回
        BeanUtils.copyProperties(skuInfo, skuInfoVo);
        skuInfoVo.setSkuImagesList(skuImageList);
        skuInfoVo.setSkuPosterList(skuPosterList);
        skuInfoVo.setSkuAttrValueList(skuAttrValueList);

        return skuInfoVo;
    }


    @Override
    public void updateSkuInfo(SkuInfoVo skuInfoVo) {
        //修改基本信息
        SkuInfo skuInfo = new SkuInfo();
        BeanUtils.copyProperties(skuInfoVo,skuInfo);
        baseMapper.updateById(skuInfo);
        //修改海报信息
        Long id = skuInfoVo.getId();
        LambdaQueryWrapper<SkuPoster> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SkuPoster::getSkuId,id);
        skuPosterService.remove(wrapper);

        List<SkuPoster> skuPosterList = skuInfoVo.getSkuPosterList();
        if (!CollectionUtils.isEmpty(skuPosterList)){
            for (SkuPoster skuPoster : skuPosterList) {
                skuPoster.setSkuId(skuInfo.getId());
            }
            skuPosterService.saveBatch(skuPosterList);
        }
        //修改图片信息
        skuImagesService.remove(new LambdaQueryWrapper<SkuImage>().eq(SkuImage::getSkuId,id));

        List<SkuImage> skuImagesList = skuInfoVo.getSkuImagesList();
        if (!CollectionUtils.isEmpty(skuImagesList)){
            for (SkuImage skuImage : skuImagesList) {
                skuImage.setSkuId(skuInfo.getId());
            }
            skuImagesService.saveBatch(skuImagesList);
        }
        //修改属性列表
        skuAttrValueService.remove(new LambdaQueryWrapper<SkuAttrValue>().eq(SkuAttrValue::getSkuId,id));

        List<SkuAttrValue> skuAttrValueList = skuInfoVo.getSkuAttrValueList();
        if (!CollectionUtils.isEmpty(skuAttrValueList)){
            for (SkuAttrValue skuAttrValue : skuAttrValueList) {
                skuAttrValue.setSkuId(skuInfo.getId());
            }
            skuAttrValueService.saveBatch(skuAttrValueList);
        }
    }

    @Override
    public void check(Long skuId, Integer status) {
        // 创建一个 SkuInfo 对象用于更新
        SkuInfo skuInfoUp = new SkuInfo();

        skuInfoUp.setId(skuId);  // 设置要更新的 SKU 的 ID
        skuInfoUp.setCheckStatus(status);  // 设置审核状态（0 表示未审核，1 表示已审核）

        // 更新 SKU 信息
        baseMapper.updateById(skuInfoUp);
    }


    @Override
    public void publish(Long skuId, Integer status) {
        // 根据传入的状态，设置SKU的发布状态
        SkuInfo skuInfoUp = new SkuInfo();
        skuInfoUp.setId(skuId);  // 设置SKU ID

        if (status == 1) {  // 如果状态为1，表示上架
            skuInfoUp.setPublishStatus(1);  // 设置发布状态为上架
            skuInfoMapper.updateById(skuInfoUp);  // 更新SKU信息

            // 商品上架，发送MQ消息以更新ES数据
            rabbitService.sendMessage(MqConst.EXCHANGE_GOODS_DIRECT, MqConst.ROUTING_GOODS_UPPER, skuId);
        } else {
            skuInfoUp.setPublishStatus(0);  // 设置发布状态为下架
            skuInfoMapper.updateById(skuInfoUp);  // 更新SKU信息

            // 商品下架，发送MQ消息以更新ES数据
            rabbitService.sendMessage(MqConst.EXCHANGE_GOODS_DIRECT, MqConst.ROUTING_GOODS_LOWER, skuId);
        }
    }


    @Override
    public void isNewPerson(Long skuId, Integer status) {
        // 创建SkuInfo对象并设置新人专享状态
        SkuInfo skuInfoUp = new SkuInfo();
        skuInfoUp.setId(skuId);  // 设置SKU ID
        skuInfoUp.setIsNewPerson(status);  // 设置新人专享状态
        // 更新数据库中的记录
        skuInfoMapper.updateById(skuInfoUp);
    }


    //批量获取sku信息
    @Override
    public List<SkuInfo> findSkuInfoList(List<Long> skuIdList) {
        return this.listByIds(skuIdList);
    }

    //根据关键字获取sku列表
    @Override
    public List<SkuInfo> findSkuInfoByKeyword(String keyword) {
        // 创建Lambda查询条件包装器
        LambdaQueryWrapper<SkuInfo> queryWrapper = new LambdaQueryWrapper<>();
        // 添加模糊查询条件：SKU名称包含关键字
        queryWrapper.like(SkuInfo::getSkuName, keyword);
        // 使用查询条件进行查询并返回结果列表
        return baseMapper.selectList(queryWrapper);
    }


    @Override
    public List<SkuInfo> findNewPersonList() {
        // 创建Lambda查询条件包装器
        LambdaQueryWrapper<SkuInfo> wrapper = new LambdaQueryWrapper<>();
        // 过滤条件：是否新人专享
        wrapper.eq(SkuInfo::getIsNewPerson, 1);
        // 过滤条件：是否上架状态
        wrapper.eq(SkuInfo::getPublishStatus, 1);
        // 根据库存降序排列
        wrapper.orderByDesc(SkuInfo::getStock);
        // 分页查询：获取第一页的前三条记录
        Page<SkuInfo> pageParam = new Page<>(1, 3);
        Page<SkuInfo> skuInfoPage = baseMapper.selectPage(pageParam, wrapper);
        // 返回分页结果中的记录列表
        return skuInfoPage.getRecords();
    }


    @Override
    public Boolean checkAndLock(List<SkuStockLockVo> skuStockLockVoList, String orderNo) {
        //判断集合是否为空
        if (CollectionUtils.isEmpty(skuStockLockVoList)){
            throw new MallException(ResultCodeEnum.DATA_ERROR);
        }
        //遍历集合得到商品 验证并锁定库存
        skuStockLockVoList.stream().forEach(skuStockLockVo -> this.checkLock(skuStockLockVo));
        //一个商品锁定失败全部锁定的商品都解锁
        boolean flag = skuStockLockVoList.stream().anyMatch(skuStockLockVo -> !skuStockLockVo.getIsLock());
        if (flag){
            skuStockLockVoList.stream().filter(SkuStockLockVo::getIsLock)
                    .forEach(skuStockLockVo -> baseMapper.unlockStock(skuStockLockVo.getSkuId(),skuStockLockVo.getSkuNum()));
            return false;
        }
        //所有商品锁定成功 redis缓存相关数据 方便解锁和减库存
        redisTemplate.opsForValue().set(RedisConst.SROCK_INFO+orderNo,skuStockLockVoList);
        return true;
    }

    //扣减库存
    @Override
    public void minusStock(String orderNo) {
        // 从缓存中获取锁定库存的信息
        List<SkuStockLockVo> skuStockLockVoList = (List<SkuStockLockVo>) this.redisTemplate.opsForValue().get(RedisConst.SROCK_INFO + orderNo);
        if (CollectionUtils.isEmpty(skuStockLockVoList)) {
            // 如果没有锁定的库存信息，直接返回
            return;
        }
        // 遍历锁定的库存信息，执行减库存操作
        skuStockLockVoList.forEach(skuStockLockVo -> {
            baseMapper.minusStock(skuStockLockVo.getSkuId(), skuStockLockVo.getSkuNum());
        });
        // 解锁库存后，从缓存中删除锁定库存的信息，防止重复解锁
        this.redisTemplate.delete(RedisConst.SROCK_INFO + orderNo);
    }


    //验证并锁定库存
    private void checkLock(SkuStockLockVo skuStockLockVo) {
        // 获取锁，这里使用公平锁
        RLock rLock = redissonClient.getFairLock(RedisConst.SKUKEY_PREFIX + skuStockLockVo.getSkuId());
        // 上锁
        rLock.lock();
        try {
            // 验证库存
            SkuInfo skuInfo = baseMapper.checkStock(skuStockLockVo.getSkuId(), skuStockLockVo.getSkuNum());
            // 判断库存是否足够
            if (skuInfo == null) {
                // 库存不足，不锁定库存
                skuStockLockVo.setIsLock(false);
                return;
            }
            // 满足条件，锁定库存
            Integer rows = baseMapper.lockStock(skuStockLockVo.getSkuId(), skuStockLockVo.getSkuNum());
            if (rows == 1) {
                // 锁定库存成功
                skuStockLockVo.setIsLock(true);
            }
        } finally {
            // 解锁
            rLock.unlock();
        }
    }



}
