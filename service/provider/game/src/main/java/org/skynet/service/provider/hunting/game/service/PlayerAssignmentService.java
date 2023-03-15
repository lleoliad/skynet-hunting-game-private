package org.skynet.service.provider.hunting.game.service;

import org.skynet.commons.lang.common.Result;
import org.skynet.components.hunting.game.query.OpenChestQuery;
import org.skynet.components.hunting.user.data.ClientUserData;
import org.skynet.service.provider.hunting.game.query.PlayerPreviewQuery;

public interface PlayerAssignmentService {
    Result<ClientUserData> preview(PlayerPreviewQuery playerPreviewQuery);
}
