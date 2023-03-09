package org.skynet.service.provider.hunting.game.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.skynet.commons.hunting.user.data.ClientUserData;
import org.skynet.commons.lang.common.Result;
import org.skynet.components.hunting.game.query.OpenChestQuery;
import org.skynet.service.provider.hunting.game.service.ChestAssignmentService;
import org.skynet.service.provider.hunting.obsolete.service.ChestService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

@Slf4j
@Service
public class ChestAssignmentServiceImpl implements ChestAssignmentService {

    @Resource
    private ChestService chestService;

    @Override
    public Result<ClientUserData> open(OpenChestQuery openChestQuery) {
        return null;
    }
}
