package org.skynet.service.provider.hunting.game.service.impl;

import cn.hutool.core.bean.BeanUtil;
import lombok.extern.slf4j.Slf4j;
import org.skynet.commons.lang.common.Result;
import org.skynet.components.hunting.game.query.OpenChestQuery;
import org.skynet.components.hunting.user.dao.entity.UserData;
import org.skynet.components.hunting.user.data.ClientUserData;
import org.skynet.components.hunting.user.data.HistoryVO;
import org.skynet.components.hunting.user.domain.ChestData;
import org.skynet.components.hunting.user.query.UserDataLandQuery;
import org.skynet.components.hunting.user.query.UserDataUpdateQuery;
import org.skynet.components.hunting.user.service.UserFeignService;
import org.skynet.service.provider.hunting.game.query.PlayerPreviewQuery;
import org.skynet.service.provider.hunting.game.service.PlayerAssignmentService;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.ChestOpenResult;
import org.skynet.service.provider.hunting.obsolete.service.ChestService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class PlayerAssignmentServiceImpl implements PlayerAssignmentService {

    @Override
    public Result<ClientUserData> preview(PlayerPreviewQuery playerPreviewQuery) {
        return null;
    }
}
