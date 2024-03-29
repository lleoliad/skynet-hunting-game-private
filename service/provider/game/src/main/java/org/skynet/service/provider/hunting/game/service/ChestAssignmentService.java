package org.skynet.service.provider.hunting.game.service;

import com.alibaba.fastjson2.JSONObject;
import org.skynet.components.hunting.game.data.ChestOpenResult;
import org.skynet.components.hunting.game.data.OpenChestBO;
import org.skynet.components.hunting.game.query.GunChestQuery;
import org.skynet.components.hunting.game.query.WinChestQuery;
import org.skynet.components.hunting.user.dao.entity.UserData;
import org.skynet.components.hunting.user.data.ClientUserData;
import org.skynet.commons.lang.common.Result;
import org.skynet.components.hunting.game.query.OpenChestQuery;
import org.skynet.components.hunting.user.domain.ChapterWinChestData;

public interface ChestAssignmentService {

    ChapterWinChestData getChapterWinChestData(UserData userData, Integer chestType, Integer chestLevel);

    Result<OpenChestBO> open(OpenChestQuery openChestQuery);

    Result<?> winChest(WinChestQuery winChestQuery);

    Result<?> gunChest(GunChestQuery gunChestQuery);
}
