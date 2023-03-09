package org.skynet.service.provider.hunting.game.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.skynet.commons.hunting.user.data.ClientUserData;
import org.skynet.commons.lang.common.Result;
import org.skynet.components.hunting.game.query.OpenChestQuery;
import org.skynet.service.provider.hunting.game.service.ChestAssignmentService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Api(tags = "宝箱管理模块")
@Slf4j
@RestController
@CrossOrigin
@RequestMapping("/skynet/service/provider/hunting/game/chest")
public class ChestAssignmentController {

    @Resource
    private ChestAssignmentService chestAssignmentService;

    @ApiOperation(value = "玩家登录", notes = "")
    @PostMapping(value = "/open")
    @ResponseBody
    public Result<ClientUserData> open(@ApiParam(name = "openChestQuery", value = "开宝箱参数", required = true) @RequestBody OpenChestQuery openChestQuery, HttpServletRequest request) {
        return chestAssignmentService.open(openChestQuery);
    }
}
