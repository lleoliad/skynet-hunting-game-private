package org.skynet.service.provider.hunting.game.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.skynet.commons.lang.common.Result;
import org.skynet.components.hunting.game.query.OpenChestQuery;
import org.skynet.components.hunting.rank.league.query.InfoPreviewQuery;
import org.skynet.components.hunting.user.data.ClientUserData;
import org.skynet.service.provider.hunting.game.query.PlayerPreviewQuery;
import org.skynet.service.provider.hunting.game.service.ChestAssignmentService;
import org.skynet.service.provider.hunting.game.service.PlayerAssignmentService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Api(tags = "宝箱管理模块")
@Slf4j
@RestController
@CrossOrigin
@RequestMapping("/skynet/service/provider/hunting/game/player")
public class PlayerAssignmentController {

    @Resource
    private PlayerAssignmentService playerAssignmentService;

    @ApiOperation(value = "获取预览信息", notes = "")
    @PostMapping(value = "/preview")
    @ResponseBody
    public Result<?> preview(@ApiParam(name = "playerPreviewQuery", value = "获取预览信息参数", required = true) @RequestBody PlayerPreviewQuery playerPreviewQuery, HttpServletRequest request) {
        return playerAssignmentService.preview(playerPreviewQuery);
    }
}
