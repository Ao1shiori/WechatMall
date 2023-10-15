package com.mall.wxw.common.result;

import lombok.Data;

/**
 * @author: wxw24633
 * @Time: 2023/10/14  21:20
 */
@Data
public class Result<T> {

    //状态码
    private Integer code;

    //信息
    private String message;

    //数据
    private T data;

    //构造私有化
    private Result() { }

    //设置数据,返回对象的方法
    public static<T> Result<T> build(Integer code, String message, T data) {
        //创建Result对象，设置值，返回对象
        Result<T> result = new Result<>();
        //判断返回结果中是否需要数据
        if(data != null) {
            //设置数据到result对象
            result.setData(data);
        }
        //设置其他值
        result.setCode(code);
        result.setMessage(message);
        //返回设置值之后的对象
        return result;
    }

    //成功的方法
    public static<T> Result<T> ok(T data) {
        return build(ResultCodeEnum.SUCCESS.getCode(),ResultCodeEnum.SUCCESS.getMessage(),data);
    }

    //失败的方法
    public static<T> Result<T> fail(T data) {
        return build(ResultCodeEnum.FAIL.getCode(),ResultCodeEnum.FAIL.getMessage(),data);
    }

}
