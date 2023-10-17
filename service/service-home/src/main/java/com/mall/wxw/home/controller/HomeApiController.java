package com.mall.wxw.home.controller;

import com.mall.wxw.common.auth.AuthContextHolder;
import com.mall.wxw.common.result.Result;
import com.mall.wxw.home.service.HomeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author: wxw24633
 * @Time: 2023/10/17  12:05
 */
@Api(tags = "首页接口")
@RestController
@RequestMapping("api/home")
public class HomeApiController {

    @Resource
    private HomeService homeService;

    @GetMapping("index")
    @ApiOperation("首页数据显示")
    public Result index(HttpServletRequest request){
        Long userId = AuthContextHolder.getUserId();
        Map<String, Object> home = homeService.home(userId);
        return Result.ok(home);
    }


}
