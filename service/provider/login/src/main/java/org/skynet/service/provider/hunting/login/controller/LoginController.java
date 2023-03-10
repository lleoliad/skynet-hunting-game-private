package org.skynet.service.provider.hunting.login.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.skynet.commons.lang.common.Result;
import org.skynet.components.hunting.user.data.ClientUserData;
import org.skynet.service.provider.hunting.login.query.LoginQuery;
import org.skynet.service.provider.hunting.login.service.LoginService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Api(tags = "宝箱管理模块")
@Slf4j
@RestController
@CrossOrigin
@RequestMapping("/skynet/service/provider/hunting/game/login")
public class LoginController {

    @Resource
    private LoginService loginService;

    @ApiOperation(value = "玩家登录", notes = "")
    @PostMapping
    @ResponseBody
    public Result<?> login(@ApiParam(name = "loginQuery", value = "玩家登录参数", required = true) @RequestBody LoginQuery loginQuery, HttpServletRequest request) {
        return loginService.login(loginQuery);
    }
}
