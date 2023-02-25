package org.skynet.service.provider.hunting.obsolete.common;

import org.skynet.service.provider.hunting.obsolete.pojo.entity.RangeInt;

/**
 * 路径的前缀后缀
 */
public class Path {

    public static String getDefaultAiProfileCollectionPath() {

        return "AiProfiles";
    }

    public static String getVerifiedPlayerControlRecordCollectionPath(String gameVersion) {

        Integer internalVersion = getPlayerControlRecordInternalVersion(gameVersion);

        return "PlayerControlRecords:VerifyRejectedHistory_" + internalVersion;
    }

    public static String getPlayerControlRecordDistributionDatabaseCollectionPath(String gameVersion) {

        Integer internalVersion = getPlayerControlRecordInternalVersion(gameVersion);

        return "PlayerControlRecordsDistribution:Version_" + internalVersion;
    }

    public static Integer getPlayerControlRecordInternalVersion(String gameVersion) {

//        String[] split = gameVersion.split("\\.");
//        int parseInt = Integer.parseInt(split[2]);
        return 5;

//        if ("1.0.11".equals(gameVersion)){
//            return 5;
//        }
//
//        ClientGameVersion clientGameVersion = ClientGameVersion.toClientGameVersionEnum(gameVersion);
//        if (clientGameVersion.getVersion().compareTo(ClientGameVersion._1_0_4_.getVersion())<0){
//            return 1;
//        }
//        if (clientGameVersion.getVersion().compareTo(ClientGameVersion._1_0_5_.getVersion())<0){
//            return 2;
//        }
//        if (clientGameVersion.getVersion().compareTo(ClientGameVersion._1_0_6_.getVersion())<0){
//            return 3;
//        }
//        if (clientGameVersion.getVersion().compareTo(ClientGameVersion._1_0_9_.getVersion())<0){
//            return 4;
//        }
//        else {
//            return 5;
//        }
    }

    public static String createPlayerControlRecordDistributionDataDocName(Long routeUid, Integer animalId, Integer gunId, Integer gunLevel, Integer bulletId) {

        return routeUid + "_" + animalId + "_" + "_" + gunId + "_" + gunLevel + "_" + bulletId;
    }

    public static String createPlayerControlRecordDistributionDataDocPath(String gameVersion, Long routeUid, Integer animalId, Integer gunId, Integer gunLevel, Integer bulletId) {

        String collectionPath = getVerifiedPlayerControlRecordCollectionPath(gameVersion);

        return "PlayerControlRecordsDistribution" + ":" + createPlayerControlRecordDistributionDataDocName(routeUid, animalId, gunId, gunLevel, bulletId);
    }

    /**
     * 生成游戏版本库名
     *
     * @param gameVersion
     * @return
     */
    public static String getHuntingMatchNowCollectionPath(String gameVersion) {
        return "HuntingMatchNowData:" + gameVersion;
    }


    public static String getHuntingMatchHistoryCollectionPath(String gameVersion) {

        return "HuntingMatchHistory:" + gameVersion;
    }

    /**
     * 获得还没有验证的玩家操作录像集合路径
     *
     * @param gameVersion
     * @return
     */
    public static String getNotVerifiedPlayerControlRecordCollectionPath(String gameVersion) {

        int internalVersion = getPlayerControlRecordInternalVersion(gameVersion);

        return "PlayerControlRecords:NotVerified_" + internalVersion;
    }

    public static String getPlayerChestOpenResultDocPath(String userUid) {

        return "UserChestOpenResult:" + userUid;
    }


    /**
     * 示例: MatchControlRecordsPool:3:101:700-800
     * 匹配池的属性路径
     * 该collection会存一些数据，例如对应collection分组的当前最高最低分等等
     *
     * @param gameVersion
     * @param matchId
     * @param trophyRange
     * @return
     */

    public static String getMatchControlRecordsPool(String gameVersion, Integer matchId, RangeInt trophyRange) {

        int internalVersion = getPlayerControlRecordInternalVersion(gameVersion);

        return "MatchControlRecordsPool:" + internalVersion + ":" + matchId + ":" + trophyRange.getMin() + "-" + trophyRange.getMax();
    }

    /**
     * MatchControlRecordsPoolTrophySegmentMetaData_3:101:700-800
     * 匹配池
     *
     * @param gameVersion
     * @param matchId
     * @param trophyRange
     * @return
     */
    public static String getMatchControlRecordsPoolTrophySegmentMetaData(String gameVersion, Integer matchId, RangeInt trophyRange) {

        int internalVersion = getPlayerControlRecordInternalVersion(gameVersion);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("MatchControlRecordsPoolTrophySegmentMetaData_");
        stringBuffer.append(internalVersion);
        stringBuffer.append(":");
        stringBuffer.append(matchId);
        stringBuffer.append(":");
        stringBuffer.append(trophyRange.getMin());
        stringBuffer.append("-");
        stringBuffer.append(trophyRange.getMax());
        return stringBuffer.toString();
    }


    /**
     * 示例: playerChapterLatestMatchScore:3:"userUid":"chapterId"
     * 保存玩家对应章节，最近几场比赛分数的集合路径
     *
     * @param gameVersion
     * @param userUid
     * @param chapterId
     * @return
     */
    public static String getPlayerChapterLatestMatchScoreCollectionPath(String gameVersion, String userUid, Integer chapterId) {

        int internalVersion = getPlayerControlRecordInternalVersion(gameVersion);
        return "playerChapterLatestMatchScore_" + internalVersion + ":" + userUid + ":" + chapterId;
    }

    /**
     * 保存玩家对应章节，最近匹配到的录像集合
     *
     * @param gameVersion
     * @param userUid
     * @param chapterId
     * @return
     */
    public static String getPlayerChapterLatestMatchIDsCollectionPath(String gameVersion, String userUid, Integer chapterId) {
        int internalVersion = getPlayerControlRecordInternalVersion(gameVersion);
        return "playerChapterLatestMatchIDs_" + internalVersion + ":" + userUid + ":" + chapterId;
    }

    /**
     * 录像池子key
     *
     * @param gameVersion
     * @return
     */
    public static String getMatchControlRecordsArchivedCollectionPath(String gameVersion) {

        int internalVersion = getPlayerControlRecordInternalVersion(gameVersion);

        return "MatchControlRecordsArchived_" + internalVersion;
    }

    /**
     * 单回合录像池子的key
     *
     * @param gameVersion
     * @param animalId
     * @param animalRouteUid
     * @param gunId
     * @param gunLevel
     * @param bulletId
     * @return
     */
    public static String getMatchRoundControlRecordsPoolCollectionPath(String gameVersion, Integer animalId, Long animalRouteUid, Integer gunId, Integer gunLevel, Integer bulletId) {


        int internalVersion = getPlayerControlRecordInternalVersion(gameVersion);
        String path = "MatchRoundControlRecordsArchived_" + internalVersion;
        String gap = ":";
        String key = path + gap +
                "animalId" + animalId + gap +
                "animalRouteUid" + animalRouteUid + gap +
                "gunId" + gunId + gap +
                "gunLevel" + gunLevel + gap +
                "bulletId" + bulletId;
        return key;
    }

    public static String getMatchRoundControlRecordsPoolCollectionPath(String gameVersion, Integer animalId, Long animalRouteUid, Integer gunId, Integer gunLevel, Integer bulletId, Integer windId) {


        int internalVersion = getPlayerControlRecordInternalVersion(gameVersion);
        String path = "MatchRoundControlRecordsArchived_" + internalVersion;
        String gap = ":";
        String key = path + gap +
                "animalId" + animalId + gap +
                "animalRouteUid" + animalRouteUid + gap +
                "gunId" + gunId + gap +
                "gunLevel" + gunLevel + gap +
                "bulletId" + bulletId + gap +
                "windId" + windId;
        return key;
    }


    public static String getMatchRoundControlRecordsPoolCollectionPath(String gameVersion, Integer animalId, Long animalRouteUid, Integer gunId, Integer gunLevel, Integer bulletId, Integer windId, Double averageShowPrecision) {


        int internalVersion = getPlayerControlRecordInternalVersion(gameVersion);
        int precisionVersion = getPrecisionVersionByAverageShowPrecision(averageShowPrecision);
        String path = "MatchRoundControlRecordsArchived_" + internalVersion;
        String gap = ":";
        String key = path + gap +
                "animalId" + animalId + gap +
                "animalRouteUid" + animalRouteUid + gap +
                "gunId" + gunId + gap +
                "gunLevel" + gunLevel + gap +
                "bulletId" + bulletId + gap +
                "windId" + windId + gap +
                "averageShowPrecision" + precisionVersion;
        return key;
    }

    private static int getPrecisionVersionByAverageShowPrecision(Double averageShowPrecision) {
        if (averageShowPrecision < 0.1) {
            return 1;
        } else if (averageShowPrecision < 0.2) {
            return 2;
        } else if (averageShowPrecision < 0.3) {
            return 3;
        } else if (averageShowPrecision < 0.4) {
            return 4;
        } else if (averageShowPrecision < 0.5) {
            return 5;
        } else if (averageShowPrecision < 0.6) {
            return 6;
        } else if (averageShowPrecision < 0.7) {
            return 7;
        } else if (averageShowPrecision < 0.8) {
            return 8;
        } else if (averageShowPrecision < 0.9) {
            return 9;
        } else if (averageShowPrecision < 1) {
            return 10;
        } else {
            return 11;
        }

    }

    /**
     * PlayerControlRecordDocData的单回合录像池子key
     *
     * @param gameVersion
     * @param animalId
     * @param animalRouteUid
     * @param gunId
     * @param gunLevel
     * @param bulletId
     * @return
     */
    public static String getMatchSingleRoundControlRecordsPoolCollectionPath(String gameVersion, Integer animalId, Long animalRouteUid, Integer gunId, Integer gunLevel, Integer bulletId) {


        int internalVersion = getPlayerControlRecordInternalVersion(gameVersion);
        String path = "MatchSingleRoundControlRecordsArchived_" + internalVersion;
        String gap = ":";
        String key = path + gap +
                "animalId" + animalId + gap +
                "animalRouteUid" + animalRouteUid + gap +
                "gunId" + gunId + gap +
                "gunLevel" + gunLevel + gap +
                "bulletId" + bulletId;
        return key;
    }

    public static String getMatchSingleRoundControlRecordsPoolCollectionPath(String gameVersion, Integer animalId, Long animalRouteUid, Integer gunId, Integer gunLevel, Integer bulletId, Integer windId) {


        int internalVersion = getPlayerControlRecordInternalVersion(gameVersion);
        String path = "MatchSingleRoundControlRecordsArchived_" + internalVersion;
        String gap = ":";
        String key = path + gap +
                "animalId" + animalId + gap +
                "animalRouteUid" + animalRouteUid + gap +
                "gunId" + gunId + gap +
                "gunLevel" + gunLevel + gap +
                "bulletId" + bulletId + gap +
                "windId" + windId;
        return key;
    }

    public static String getMatchSingleRoundControlRecordsPoolCollectionPath(String gameVersion, Integer animalId, Long animalRouteUid, Integer gunId, Integer gunLevel, Integer bulletId, Integer windId, Double averageShowPrecision) {


        int internalVersion = getPlayerControlRecordInternalVersion(gameVersion);
        int precisionVersion = getPrecisionVersionByAverageShowPrecision(averageShowPrecision);
        String path = "MatchSingleRoundControlRecordsArchived_" + internalVersion;
        String gap = ":";
        String key = path + gap +
                "animalId" + animalId + gap +
                "animalRouteUid" + animalRouteUid + gap +
                "gunId" + gunId + gap +
                "gunLevel" + gunLevel + gap +
                "bulletId" + bulletId + gap +
                "windId" + windId + gap +
                "averageShowPrecision" + precisionVersion;
        return key;
    }


    public static String getMatchSingleRoundControlRecordsPoolCollectionPath(String gameVersion, Integer animalId, Long animalRouteUid, Integer gunId, Integer gunLevel, Integer bulletId, Integer windId, Integer precisionVersion) {

        int internalVersion = getPlayerControlRecordInternalVersion(gameVersion);
        String path = "MatchSingleRoundControlRecordsArchived_" + internalVersion;
        String gap = ":";
        String key = path + gap +
                "animalId" + animalId + gap +
                "animalRouteUid" + animalRouteUid + gap +
                "gunId" + gunId + gap +
                "gunLevel" + gunLevel + gap +
                "bulletId" + bulletId + gap +
                "windId" + windId + gap +
                "averageShowPrecision" + precisionVersion;
        return key;
    }

    /**
     * 游客账号map数据集合路径
     *
     * @return
     */
    public static String getGuestAccountMapDataCollectionPath() {

        return "GuestAccountMap";
    }

    public static String getUserInboxMailCollectionPath() {

        return "UserMail:UserMailInbox";
    }

    public static String getInboxMailCollectionPath() {

        return "UserMailInbox";
    }

    public static String getArchivedMailCollectionPath() {

        return "ArchivedUserMail";
    }

    public static String getUserArchivedMailCollectionPath() {

        return "UserMail:ArchivedUserMail";
    }

    /**
     * google账号map数据集合路径
     *
     * @return
     */
    public static String getGoogleAccountMapDataCollectionPath() {

        return "GoogleAccountMap";
    }

    /**
     * facebook账号map数据集合路径
     *
     * @return
     */
    public static String getFacebookAccountMapDataCollectionPath() {
        return "FacebookAccountMap";
    }


    /**
     * 用户头像路径
     *
     * @return
     */
    public static String getUserProfileImageCollectionPath() {

        return "UserProfileImage";
    }

    public static String getUserServerResponseArchiveCollectionPath(String userUid) {

        return "ServerResponseArchive/" + userUid + "/ServerResponseArchive";
    }


    public static String getUserLoginSessionDataDocPath(String userUid) {

        return "UserLoginSession/" + userUid;
    }

    /**
     * 完成的订单
     *
     * @return
     */
    public static String getCompletedIAPOrderCollectionPath() {

        return "CompletedIapOrders";
    }

    /**
     * 处理中的订单
     *
     * @return
     */
    public static String getPendingCustomOrderCollectionPath() {

        return "PendingCustomOrders";
    }

    /**
     * 压缩的订单
     *
     * @return
     */
    public static String getArchiveCustomOrderCollectionPath() {

        return "CompleteCustomOrders";
    }

}
