package org.skynet.service.provider.hunting.game.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.skynet.commons.lang.common.Result;
import org.skynet.components.hunting.game.query.MatchConsumeBulletQuery;
import org.skynet.components.hunting.game.query.MatchCompleteQuery;
import org.skynet.service.provider.hunting.game.service.MatchAssignmentService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Api(tags = "宝箱管理模块")
@Slf4j
@RestController
@CrossOrigin
@RequestMapping("/skynet/service/provider/hunting/game/match")
public class MatchAssignmentController {

    @Resource
    private MatchAssignmentService matchAssignmentService;

    @ApiOperation(value = "消耗子弹", notes = "")
    @PostMapping(value = "/consumeBullet")
    @ResponseBody
    public Result<?> consumeBullet(@ApiParam(name = "matchConsumeBulletQuery", value = "消耗子弹参数", required = true) @RequestBody MatchConsumeBulletQuery matchConsumeBulletQuery, HttpServletRequest request) {
        return matchAssignmentService.consumeBullet(matchConsumeBulletQuery);
    }

    @ApiOperation(value = "比赛完成", notes = "")
    @PostMapping(value = "/complete")
    @ResponseBody
    public Result<?> complete(@ApiParam(name = "matchCompleteQuery", value = "比赛完成参数", required = true) @RequestBody MatchCompleteQuery matchCompleteQuery, HttpServletRequest request) {
        return matchAssignmentService.complete(matchCompleteQuery);
    }
}
