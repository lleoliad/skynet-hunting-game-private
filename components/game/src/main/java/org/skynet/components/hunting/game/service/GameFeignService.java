package org.skynet.components.hunting.game.service;

import io.swagger.annotations.ApiParam;
import org.skynet.commons.lang.common.Result;
import org.skynet.components.hunting.game.query.OpenChestQuery;
import org.skynet.components.hunting.user.data.ClientUserData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Component
@FeignClient(value = "${skynet.service-list.game.url}/skynet/service/provider/hunting/game")
public interface GameFeignService {
    @PostMapping(value = "chest/open")
    Result<ClientUserData> chestOpen(@ApiParam(name = "openChestQuery", value = "开宝箱参数", required = true) @RequestBody OpenChestQuery openChestQuery);

}
