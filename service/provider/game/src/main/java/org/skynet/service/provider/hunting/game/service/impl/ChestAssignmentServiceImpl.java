package org.skynet.service.provider.hunting.game.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.skynet.components.hunting.user.dao.entity.UserData;
import org.skynet.components.hunting.user.data.ClientUserData;
import org.skynet.commons.lang.common.Result;
import org.skynet.components.hunting.game.query.OpenChestQuery;
import org.skynet.components.hunting.user.domain.ChestData;
import org.skynet.components.hunting.user.query.UserDataLandQuery;
import org.skynet.components.hunting.user.query.UserDataUpdateQuery;
import org.skynet.components.hunting.user.service.UserFeignService;
import org.skynet.service.provider.hunting.game.service.ChestAssignmentService;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.ChestOpenResult;
import org.skynet.service.provider.hunting.obsolete.service.ChestService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class ChestAssignmentServiceImpl implements ChestAssignmentService {

    @Resource
    private UserFeignService userFeignService;

    @Resource
    private ChestService chestService;

    @Override
    public Result<ClientUserData> open(OpenChestQuery openChestQuery) {
        Result<UserData> userDataResult = userFeignService.load(UserDataLandQuery.builder().userId(openChestQuery.getUserId()).build());
        if (userDataResult.failed()) {
            return userDataResult.build();
        }

        UserData userData = userDataResult.getData();

        ChestOpenResult chestOpenResult = chestService.openChest(userDataResult.getData(), ChestData.builder().build(), openChestQuery.getVersion());

        userFeignService.update(UserDataUpdateQuery.builder().userId(openChestQuery.getUserId()).userData(userData).build());
        ClientUserData clientUserData = ClientUserData.builder().build();
        if (chestOpenResult != null) {
            clientUserData.setBulletCountMap(userData.getBulletCountMap());
            clientUserData.setCoin(userData.getCoin());
            clientUserData.setDiamond(userData.getDiamond());
            clientUserData.setGunLevelMap(userData.getGunLevelMap());
            clientUserData.setGunCountMap(userData.getGunCountMap());
        }

        return Result.ok().push("userData", clientUserData).push("openResult", chestOpenResult);
    }
}
