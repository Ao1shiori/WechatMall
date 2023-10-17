package com.mall.wxw.user.controller;

import com.alibaba.fastjson.JSONObject;
import com.mall.wxw.common.auth.AuthContextHolder;
import com.mall.wxw.common.constant.RedisConst;
import com.mall.wxw.common.exception.MallException;
import com.mall.wxw.common.result.Result;
import com.mall.wxw.common.result.ResultCodeEnum;
import com.mall.wxw.common.utils.JwtHelper;
import com.mall.wxw.enums.UserType;
import com.mall.wxw.enums.user.User;
import com.mall.wxw.user.service.UserService;
import com.mall.wxw.user.utils.ConstantPropertiesUtil;
import com.mall.wxw.user.utils.HttpClientUtils;
import com.mall.wxw.vo.user.LeaderAddressVo;
import com.mall.wxw.vo.user.UserLoginVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/user/weixin")
public class WeixinApiController {

    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate redisTemplate;

    @ApiOperation(value = "微信登录获取openid(小程序)")
    @GetMapping("/wxLogin/{code}")
    public Result callback(@PathVariable String code) {
        System.out.println("微信授权服务器回调。。。。。。"+code);
        //code appId 密钥请求微信接口服务
        String wxOpenAppId = ConstantPropertiesUtil.WX_OPEN_APP_ID;
        String wxOpenAppSecret = ConstantPropertiesUtil.WX_OPEN_APP_SECRET;
        // 拼接请求地址+参数
        String url = "https://api.weixin.qq.com/sns/jscode2session" +
                "?appid=%s" +
                "&secret=%s" +
                "&js_code=%s" +
                "&grant_type=authorization_code";
        String tokenUrl = String.format(url, ConstantPropertiesUtil.WX_OPEN_APP_ID,
                ConstantPropertiesUtil.WX_OPEN_APP_SECRET,
                code);
        //HttpClient工具请求
        String result = null;
        try {
            result = HttpClientUtils.get(tokenUrl);
        } catch (Exception e) {
            throw new MallException(ResultCodeEnum.FETCH_ACCESSTOKEN_FAILD);
        }
        System.out.println("使用code换取的access_token结果 = " + result);
        //请求后返回sessionKey和openId
        JSONObject jsonObject = JSONObject.parseObject(result);
        String session_key = jsonObject.getString("session_key");
        String openid = jsonObject.getString("openid");
        //用openid判断并添加用户信息到数据库
        //根据access_token获取微信用户的基本信息
        //先根据openid进行数据库查询
        User user = userService.getByOpenid(openid);
        // 如果没有查到用户信息,那么调用微信个人信息获取的接口
        if(null == user){
            user = new User();
            user.setOpenId(openid);
            user.setNickName(openid);
            user.setPhotoUrl("");
            user.setUserType(UserType.USER);
            user.setIsNew(0);
            userService.save(user);
        }
        //根据userId查询提货点和团长信息
        LeaderAddressVo leaderAddressVo = userService.getLeaderAddressVoByUserId(user.getId());
        //jwt根据userId和username生成token
        String token = JwtHelper.createToken(user.getId(), user.getNickName());
        //获取登录用户信息放到redis并设置有效时间
        UserLoginVo userLoginVo = userService.getUserLoginVo(user.getId());
        redisTemplate.opsForValue().set(RedisConst.USER_LOGIN_KEY_PREFIX + user.getId(), userLoginVo, RedisConst.USERKEY_TIMEOUT, TimeUnit.DAYS);
        //封装数据到map返回
        Map<String, Object> map = new HashMap<>();
        map.put("user",user);
        map.put("token",token);
        map.put("leaderAddressVo",leaderAddressVo);
        return Result.ok(map);
    }

    @PostMapping("/auth/updateUser")
    @ApiOperation(value = "更新用户昵称与头像")
    public Result updateUser(@RequestBody User user) {
        User user1 = userService.getById(AuthContextHolder.getUserId());
        //把昵称更新为微信用户
        user1.setNickName(user.getNickName().replaceAll("[ue000-uefff]", "*"));
        user1.setPhotoUrl(user.getPhotoUrl());
        userService.updateById(user1);
        return Result.ok(null);
    }
}