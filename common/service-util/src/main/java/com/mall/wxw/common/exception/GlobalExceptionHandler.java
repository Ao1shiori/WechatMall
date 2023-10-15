package com.mall.wxw.common.exception;

import com.mall.wxw.common.result.Result;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class) //异常处理器
    @ResponseBody //返回json数据
    public Result error(Exception e){
        e.printStackTrace();
        return Result.fail(null);
    }

    /**
     * 自定义异常处理方法
     */
    @ExceptionHandler(MallException.class)
    @ResponseBody
    public Result error(MallException e){
        return Result.build(e.getCode(),e.getMessage(),null);
    }
}