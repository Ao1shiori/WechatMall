package com.mall.wxw.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.wxw.enums.user.User;
import com.mall.wxw.vo.user.LeaderAddressVo;
import com.mall.wxw.vo.user.UserLoginVo;

public interface UserService extends IService<User> {

    LeaderAddressVo getLeaderAddressVoByUserId(Long userId);

    /**
     * 根据微信openid获取用户信息
     * @param openId
     * @return
     */
    User getByOpenid(String openId);

    /**
     * 获取当前登录用户信息
     * @param userId
     * @return
     */
    UserLoginVo getUserLoginVo(Long userId);
}