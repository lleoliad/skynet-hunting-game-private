package org.skynet.service.provider.hunting.game.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.skynet.commons.lang.common.Result;
import org.skynet.components.hunting.user.data.ClientUserData;
import org.skynet.service.provider.hunting.game.query.PlayerPreviewQuery;
import org.skynet.service.provider.hunting.game.service.PlayerAssignmentService;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PlayerAssignmentServiceImpl implements PlayerAssignmentService {

    @Override
    public Result<ClientUserData> preview(PlayerPreviewQuery playerPreviewQuery) {
        return null;
    }
}
