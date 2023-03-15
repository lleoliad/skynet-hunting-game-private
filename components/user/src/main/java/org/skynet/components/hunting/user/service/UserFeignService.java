package org.skynet.components.hunting.user.service;

import io.swagger.annotations.ApiParam;
import org.skynet.components.hunting.user.dao.entity.UserData;
import org.skynet.components.hunting.user.data.PlayerDetailInfoDTO;
import org.skynet.components.hunting.user.query.*;
import org.skynet.commons.lang.common.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Component
@FeignClient(value = "${skynet.service-list.user.url}/skynet/service/provider/hunting/user")
public interface UserFeignService {

    @PostMapping(value = "player/create")
    Result<UserData> create(@ApiParam(name = "playerCreateQuery", value = "玩家登录参数", required = true) @RequestBody PlayerCreateQuery playerCreateQuery);

    @PostMapping(value = "player/login")
    Result<UserData> login(@ApiParam(name = "playerLoginQuery", value = "玩家登录参数", required = true) @RequestBody PlayerLoginQuery playerLoginQuery);

    @PostMapping(value = "player/get/playerInfo")
    Result<UserData> getPlayerInfo(@ApiParam(name = "getPlayerInfoQuery", value = "获取玩家信息参数", required = true) @RequestBody GetPlayerInfoQuery getPlayerInfoQuery);

    @PostMapping(value = "player/preview")
    Result<PlayerDetailInfoDTO> preview(@ApiParam(name = "getPreviewQuery", value = "获取预览信息参数", required = true) @RequestBody GetPreviewQuery getPreviewQuery);

    @PostMapping(value = "player/load")
    Result<UserData> load(@ApiParam(name = "userDataLandQuery", value = "用户数据加载参数", required = true) @RequestBody UserDataLandQuery userDataLandQuery);

    @PostMapping(value = "player/update")
    Result<?> update(@ApiParam(name = "userDataUpdateQuery", value = "用户数据更新参数", required = true) @RequestBody UserDataUpdateQuery userDataUpdateQuery);

}
