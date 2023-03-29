package org.skynet.service.provider.hunting.game.service;

import com.alibaba.fastjson2.JSONObject;
import org.skynet.components.hunting.game.query.WinChestQuery;
import org.skynet.components.hunting.user.data.ClientUserData;
import org.skynet.commons.lang.common.Result;
import org.skynet.components.hunting.game.query.OpenChestQuery;
import org.skynet.components.hunting.user.domain.ChapterWinChestData;

public interface ChestAssignmentService {
    Result<ClientUserData> open(OpenChestQuery openChestQuery);

    Result<ChapterWinChestData> winChest(WinChestQuery winChestQuery);
}
