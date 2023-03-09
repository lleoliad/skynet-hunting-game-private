package org.skynet.service.provider.hunting.obsolete.service.impl;

import org.skynet.service.provider.hunting.obsolete.common.util.TimeUtils;
import org.skynet.commons.hunting.user.domain.ChapterBonusPackageData;
import org.skynet.commons.hunting.user.dao.entity.UserData;
import org.skynet.service.provider.hunting.obsolete.pojo.environment.GameEnvironment;
import org.skynet.service.provider.hunting.obsolete.pojo.table.ChapterBonusPackageTableValue;
import org.skynet.service.provider.hunting.obsolete.pojo.table.ChapterGunGiftPackageTableValue;
import org.skynet.service.provider.hunting.obsolete.service.ChapterBonusPackageDataService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ChapterBonusPackageDataServiceImpl implements ChapterBonusPackageDataService {


    /**
     * 检查是否有章节礼包过期
     */
    @Override
    public void checkChapterBonusPackageExpire(String uuid) {

        UserData userData = GameEnvironment.userDataMap.get(uuid);
        List<ChapterBonusPackageData> chapterBonusPackagesData = userData.getChapterBonusPackagesData();
        Long unixTimeNow = TimeUtils.getUnixTimeSecond();

        List<ChapterBonusPackageData> expiredPackagesData = new ArrayList<>();

        chapterBonusPackagesData.forEach(packageData -> {
            if (packageData.getIsActive() && packageData.getExpireTime() < unixTimeNow) {
                expiredPackagesData.add(packageData);
            }
        });

        chapterBonusPackagesData = chapterBonusPackagesData.stream().filter(value -> !expiredPackagesData.contains(value)).collect(Collectors.toList());

        userData.setChapterBonusPackagesData(chapterBonusPackagesData);
    }

    @Override
    public ChapterBonusPackageData createChapterBonusPackageData(UserData userData, Integer chapterId, String gameVersion) {

        if (userData.getServerOnly().getChapterIdsOfObtainedChapterBonusPackage().contains(chapterId)) {
            return null;
        }

        Map<String, ChapterBonusPackageTableValue> chapterBonusPackageTable = GameEnvironment.chapterBonusPackageTableMap.get(gameVersion);
        ChapterBonusPackageTableValue tableValue = chapterBonusPackageTable.get(String.valueOf(chapterId));

        if (tableValue == null) {
            //新增，5-12章的礼包单独配置，如果之前的礼包数据为空，再判断新表中的数据，如果同时为空，则返回null
            Map<String, ChapterGunGiftPackageTableValue> chapterGunGiftPackageTable = GameEnvironment.chapterGunGiftPackageTableMap.get(gameVersion);
            ChapterGunGiftPackageTableValue gunGiftPackageTableValue = chapterGunGiftPackageTable.get(String.valueOf(chapterId));
            if (gunGiftPackageTableValue == null) {
                return null;
            }
        }

        long unixTimeNow = TimeUtils.getUnixTimeSecond();

        ChapterBonusPackageData packageData = new ChapterBonusPackageData(chapterId, unixTimeNow, -1L, false);

        userData.getChapterBonusPackagesData().add(packageData);
        userData.getServerOnly().getChapterIdsOfObtainedChapterBonusPackage().add(chapterId);
        return packageData;
    }
}
