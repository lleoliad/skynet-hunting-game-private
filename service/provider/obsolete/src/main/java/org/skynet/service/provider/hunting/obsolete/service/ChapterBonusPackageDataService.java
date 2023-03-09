package org.skynet.service.provider.hunting.obsolete.service;

import org.skynet.commons.hunting.user.domain.ChapterBonusPackageData;
import org.skynet.commons.hunting.user.dao.entity.UserData;

public interface ChapterBonusPackageDataService {

    /**
     * 检查是否有章节礼包过期
     */
    void checkChapterBonusPackageExpire(String uuid);

    /**
     * 玩家解锁章节时,可以获得对应章节的礼包
     *
     * @param userData
     * @param chapterId
     * @param gameVersion
     * @return
     */
    ChapterBonusPackageData createChapterBonusPackageData(UserData userData, Integer chapterId, String gameVersion);
}
