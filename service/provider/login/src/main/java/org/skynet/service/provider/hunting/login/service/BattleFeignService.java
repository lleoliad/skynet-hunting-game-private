package org.skynet.service.provider.hunting.login.service;

import io.swagger.annotations.ApiParam;
import org.skynet.commons.lang.common.Result;
import org.skynet.commons.lang.common.SkynetObject;
import org.skynet.service.provider.hunting.login.query.BattleUserCreateQuery;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Component
@FeignClient(value = "${skynet.service-list.battle.url}")
public interface BattleFeignService {

    @PostMapping(value = "user/create")
    Result<SkynetObject> userCreate(@ApiParam(name = "battleUserCreateQuery", value = "战斗服务器，用户信息创建参数", required = true) @RequestBody BattleUserCreateQuery battleUserCreateQuery);

}
