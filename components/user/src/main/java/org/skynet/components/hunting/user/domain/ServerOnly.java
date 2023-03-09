package org.skynet.components.hunting.user.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.skynet.components.hunting.user.enums.ABTestGroup;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
// @Builder
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "ServerOnly对象", description = "不向客户端发送的数据")
public class ServerOnly implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "abtest 分组")
    ABTestGroup abTestGroup;

    @ApiModelProperty(value = "私钥")
    String privateKey;

    @ApiModelProperty(value = "最近一次登陆时间")
    Long latestLoginTime;

    @ApiModelProperty(value = "上一次登陆时,客户端版本")
    String lastLoginClientVersion = "";

    @ApiModelProperty(value = "宝箱内容表格index")
    ChestOpenIndexMap chestOpenIndexMap;

    @ApiModelProperty(value = "章节箱子任务表格index")
    Integer chapterChestTaskIndex = 1;

    @ApiModelProperty(value = "已经创建过的章节礼包的章节ID")
    List<Integer> chapterIdsOfObtainedChapterBonusPackage = new ArrayList<>();

    @ApiModelProperty(value = "已经购买过的活动礼包key", notes = "groupId_index")
    List<String> purchasedPromotionEventPackagesKeys = new ArrayList<>();

    @ApiModelProperty(value = "已经购买过的活动礼包key", notes = "groupId_index")
    List<String> purchasedPromotionEventPackagesV2Keys = new ArrayList<>();

    @ApiModelProperty(value = "最近匹配过的ai profiles")
    List<Integer> latestMatchedAiProfileIds = new ArrayList<>();

    @ApiModelProperty(value = "上一次每日签到钻石奖励领取时间")
    Long lastSigninDiamondRewardCollectTime = 0L;

    @ApiModelProperty(value = "签到钻石奖励获得次数")
    Integer signinDiamondRewardCollectTimes = 0;

    @ApiModelProperty(value = "玩家录制模式的数据", notes = "如果用户是录制模式客户端，则有该数据")
    private PlayerRecordModeData recordModeData;

    @ApiModelProperty("开始封禁时间")
    private long startBlockTime = -1;

    @ApiModelProperty("结束封禁时间")
    private long endBlockTime = -1;

}
