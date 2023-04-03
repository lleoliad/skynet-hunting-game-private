package org.skynet.components.hunting.game.service;

import com.alibaba.fastjson2.JSONObject;
import io.swagger.annotations.ApiParam;
import org.skynet.commons.lang.common.Result;
import org.skynet.commons.lang.common.SkynetObject;
import org.skynet.components.hunting.game.data.ChestOpenResult;
import org.skynet.components.hunting.game.query.*;
import org.skynet.components.hunting.user.data.ClientUserData;
import org.skynet.components.hunting.user.domain.ChapterWinChestData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Component
@FeignClient(value = "${skynet.service-list.game.url}/skynet/service/provider/hunting/game")
public interface GameFeignService {
    @PostMapping(value = "chest/open")
    Result<ClientUserData> chestOpen(@ApiParam(name = "openChestQuery", value = "开宝箱参数", required = true) @RequestBody OpenChestQuery openChestQuery);

    @PostMapping(value = "chest/winChest")
    Result<ChapterWinChestData> winChest(@ApiParam(name = "winChestQuery", value = "胜利宝箱参数", required = true) @RequestBody WinChestQuery winChestQuery);

    @PostMapping(value = "chest/gunChest")
    // Result<ChestOpenResult> gunChest(@ApiParam(name = "gunChestQuery", value = "枪械宝箱参数", required = true) @RequestBody GunChestQuery gunChestQuery);
    Result<SkynetObject> gunChest(@ApiParam(name = "gunChestQuery", value = "枪械宝箱参数", required = true) @RequestBody GunChestQuery gunChestQuery);

    @PostMapping(value = "match/consumeBullet")
    Result<ClientUserData> consumeBullet(@ApiParam(name = "matchConsumeBulletQuery", value = "消耗子弹参数", required = true) @RequestBody MatchConsumeBulletQuery matchConsumeBulletQuery);

    @PostMapping(value = "match/complete")
    Result<ClientUserData> complete(@ApiParam(name = "matchCompleteQuery", value = "比赛完成参数", required = true) @RequestBody MatchCompleteQuery matchCompleteQuery);

}
