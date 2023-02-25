package org.skynet.service.provider.hunting.obsolete.common.result;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 一种返回类型
 */
@Data
public class R {
    private Integer code;
    private String message;
    private Long serverTime;
    private Map<String, Object> data = new HashMap<>();

    /**
     * 构造函数私有化
     */
    private R() {
    }

    /**
     * 返回成功结果
     *
     * @return:
     */
    public static R ok() {
        R r = new R();
        r.setCode(ResponseEnum.SUCCESS.getCode());
        r.setMessage(ResponseEnum.SUCCESS.getMessage());
        return r;
    }


    /**
     * 返回失败结果
     *
     * @return:
     */
    public static R error() {
        R r = new R();
        r.setCode(ResponseEnum.ERROR.getCode());
        r.setMessage(ResponseEnum.ERROR.getMessage());
        return r;
    }

    /**
     * 设置特定结果
     *
     * @param responseEnum
     * @return:
     */
    public static R setResult(ResponseEnum responseEnum) {
        R r = new R();
        r.setCode(responseEnum.getCode());
        r.setMessage(responseEnum.getMessage());
        return r;
    }

    public R data(String key, Object value) {
        this.data.put(key, value);
        return this;
    }

    public R data(Map<String, Object> map) {
        this.setData(map);
        return this;
    }

    /**
     * 设置特定的响应消息
     *
     * @param message
     */
    public R message(String message) {
        this.setMessage(message);
        return this;
    }

    /**
     * 设置特定的响应消息
     *
     * @param code
     */
    public R code(Integer code) {
        this.setCode(code);
        return this;
    }

    /**
     * 设置时间
     *
     * @param serverTime
     * @return
     */
    public R serverTime(Long serverTime) {
        this.setServerTime(serverTime);
        return this;
    }
}
