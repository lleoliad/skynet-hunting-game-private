package org.skynet.service.provider.hunting.game.service;

import org.skynet.components.hunting.user.data.ClientUserData;
import org.skynet.commons.lang.common.Result;
import org.skynet.components.hunting.game.query.OpenChestQuery;

public interface ChestAssignmentService {
    Result<ClientUserData> open(OpenChestQuery openChestQuery);
}
