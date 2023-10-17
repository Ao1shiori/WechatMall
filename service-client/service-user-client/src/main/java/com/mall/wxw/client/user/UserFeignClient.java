package com.mall.wxw.client.user;

import com.mall.wxw.vo.user.LeaderAddressVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author: wxw24633
 * @Time: 2023/10/17  12:20
 */
@FeignClient(value = "service-user")
public interface UserFeignClient {
    @ApiOperation("提货点地址信息")
    @GetMapping("/api/user/leader/inner/getUserAddressByUserId/{userId}")
    public LeaderAddressVo getUserAddressByUserId(@PathVariable(value = "userId") Long userId);
}
