package com.mall.wxw.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.wxw.enums.user.Leader;
import com.mall.wxw.enums.user.User;
import com.mall.wxw.enums.user.UserDelivery;
import com.mall.wxw.user.mapper.LeaderMapper;
import com.mall.wxw.user.mapper.UserDeliveryMapper;
import com.mall.wxw.user.mapper.UserMapper;
import com.mall.wxw.user.service.UserService;
import com.mall.wxw.vo.user.LeaderAddressVo;
import com.mall.wxw.vo.user.UserLoginVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author: wxw24633
 * @Time: 2023/10/16  20:38
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Resource
    private LeaderMapper leaderMapper;

    @Resource
    private UserDeliveryMapper userDeliveryMapper;

    @Override
    public LeaderAddressVo getLeaderAddressVoByUserId(Long userId) {
        //查默认团长id
        UserDelivery userDelivery =
                userDeliveryMapper.selectOne(new QueryWrapper<UserDelivery>()
                        .eq("user_id", userId)
                        .eq("is_default",1));
        if (userDelivery == null){
            return null;
        }
        //根据上面查的信息查其他信息
        Leader leader = leaderMapper.selectById(userDelivery.getLeaderId());
        //封装数据
        LeaderAddressVo leaderAddressVo = new LeaderAddressVo();
        BeanUtils.copyProperties(leader, leaderAddressVo);
        leaderAddressVo.setUserId(userId);
        leaderAddressVo.setLeaderId(leader.getId());
        leaderAddressVo.setLeaderName(leader.getName());
        leaderAddressVo.setLeaderPhone(leader.getPhone());
        leaderAddressVo.setWareId(userDelivery.getWareId());
        leaderAddressVo.setStorePath(leader.getStorePath());
        return leaderAddressVo;
    }

    @Override
    public User getByOpenid(String openId) {
        return baseMapper.selectOne(new QueryWrapper<User>().eq("open_id",openId));
    }

    @Override
    public UserLoginVo getUserLoginVo(Long userId) {
        User user = this.getById(userId);
        UserLoginVo userLoginVo = new UserLoginVo();
        userLoginVo.setUserId(userId);
        userLoginVo.setNickName(user.getNickName());
        userLoginVo.setPhotoUrl(user.getPhotoUrl());
        userLoginVo.setOpenId(user.getOpenId());
        userLoginVo.setIsNew(user.getIsNew());

        UserDelivery userDelivery =
                userDeliveryMapper.selectOne(new QueryWrapper<UserDelivery>()
                        .eq("user_id", userId)
                        .eq("is_default",1));
        if (userDelivery != null){
            userLoginVo.setLeaderId(userDelivery.getLeaderId());
            userLoginVo.setWareId(userDelivery.getWareId());
        }else {
            userLoginVo.setLeaderId(1L);
            userLoginVo.setWareId(1L);
        }
        return userLoginVo;
    }
}
