package org.skynet.service.provider.hunting.obsolete.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * 返回信息和返回码
 */
@Getter
@AllArgsConstructor
@ToString
public enum ResponseEnum {

    SUCCESS(0, "成功"),
    ERROR(-1, "服务器内部错误"),
    SERVLET_ERROR(-102, "servlet请求异常"), //-2xx 参数校验
    LOGIN_AUTH_ERROR(-211, "未登录"),
    LOGIN_ERROR(-216, "登录失败"),
    CLIENT_GAME_VERSION(-212, "游戏版本不存在"),
    DATA_INSERT_ERROR(-213, "数据插入失败"),
    DATA_EXIST(-214, "数据库中已经存在该数据"),
    DATA_NOT_EXIST(-215, "数据库中不存在该数据"),
    CMD_ERROR(-3, "命令错误"),
    ;

    //响应状态码
    private Integer code;

    //响应信息
    private String message;
}
