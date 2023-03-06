package org.skynet.service.provider.hunting.obsolete.service;

import org.skynet.service.provider.hunting.obsolete.pojo.dto.BaseDTO;
import com.cn.huntingrivalserver.pojo.entity.*;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.*;
import org.skynet.service.provider.hunting.obsolete.pojo.table.ChapterTableValue;

import java.util.LinkedList;
import java.util.List;

public interface PlayerControlRecordDataService {


    /**
     * 解码
     *
     * @param encodeData
     * @return
     */
    String decodeControlRecordData(String encodeData);

    /**
     * 加密 有问题
     *
     * @param recordParsedData
     */
    String encodeControlRecordData(PlayerControlRecordData recordParsedData);


    /**
     * 获取录制操作应该放在章节的哪一个奖杯分段池中
     *
     * @param chapterId
     * @param trophyCount
     * @param gameVersion
     * @return
     */
    RangeInt getControlRecordTrophySegmentRange(Integer chapterId, Integer trophyCount, String gameVersion);

    /**
     * 获取录像池信息
     *
     * @param metaDataDocRef       匹配池属性key
     * @param segmentCollectionRef 匹配池key
     * @return
     */
    MatchControlRecordsPoolMetaData getPoolTrophySegmentMetaData(String metaDataDocRef, String segmentCollectionRef);


    /**
     * 取出匹配池里面的所有数据
     *
     * @return
     */
    List<MatchRecord> getPoolCollection(String path);

    /**
     * 获得最近玩家匹配到的对局
     *
     * @param userUid
     * @param chapterId
     * @param gameVersion
     * @return
     */
    LinkedList<String> getPlayerUsedWholeMatchControlRecordsUids(String userUid, Integer chapterId, String gameVersion);


    /**
     * 保存修改后的玩家最近匹配信息
     *
     * @param userUid
     * @param chapterId
     * @param matchUid
     * @param gameVersion
     */
    void savePlayerUsedWholeMatchControlRecordsUids(String userUid, Integer chapterId, String matchUid, String gameVersion);

    /**
     * 确认比赛结束后保存玩家录像
     *
     * @param userUid
     * @param historyData
     * @param allEncodedControlRecordsData
     * @param averageFrameRate
     * @param gameVersion
     */
    void savePlayerUploadControlRecords(BaseDTO request, String userUid, HuntingMatchHistoryData historyData, List<String> allEncodedControlRecordsData, Integer averageFrameRate, String gameVersion);

    /**
     * 根据MatchRecord中录像的索引找出所有符合条件的录像
     *
     * @param list
     * @param gameVersion
     * @return
     */
    List<PlayerUploadWholeMatchControlRecordData> getPlayerUploadWholeMatchControlRecordDataCollection(List<MatchRecord> list, String gameVersion);

    /**
     * 检查操作是否有任何有效操作。
     * 在一回合中，玩家可能没有任何操作，这种录像文件是无效文件
     *
     * @param encodeData
     * @return
     */
    Boolean checkControlRecordIsValid(String encodeData);

    /**
     * 解码单回合压缩文件并添加默认信息
     *
     * @param dataBytesBase64Encoded
     * @return
     */
    PlayerControlRecordDocData createPlayerControlRecordDocData(String dataBytesBase64Encoded);

    List<RangeInt> getChapterRecordsTrophySegmentRanges(ChapterTableValue chapterTableValue);

    void downloadPlayerWholeData(Boolean IsOpponentDisconnect, String gameVersion, PlayerUploadWholeMatchControlRecordData wholeData);

    void downloadSingleData(PlayerControlRecordDocData recordDocData);

    String decodePlayerControlRecordData(String dataBytesBase64Encoded);
}
