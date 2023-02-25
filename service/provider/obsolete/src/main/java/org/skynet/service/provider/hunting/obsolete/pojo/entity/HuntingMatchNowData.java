package org.skynet.service.provider.hunting.obsolete.pojo.entity;


import org.skynet.service.provider.hunting.obsolete.enums.HuntingMatchAIRecordChooseMode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "HuntingMatchNowData对象", description = "正在寻找的匹配数据")
public class HuntingMatchNowData implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "匹配uid")
    private String matchUid;

    @ApiModelProperty(value = "玩家id")
    private String playerUid;

    @ApiModelProperty(value = "章节id")
    private Integer chapterId;

    @ApiModelProperty(value = "录制模式下，记录玩家选择的哪一章")
    private Integer recordModeSelectChapterId;

    @ApiModelProperty(value = "匹配id")
    private Integer matchId;

    @ApiModelProperty(value = "本地玩家武器信息")
    private PlayerWeaponInfo localPlayerWeaponInfo;

    @ApiModelProperty(value = "对手玩家武器信息")
    private PlayerWeaponInfo opponentPlayerWeaponInfo;

    @ApiModelProperty(value = "开始时间")
    private Long startTime;

    @ApiModelProperty(value = "比赛过程中,ai获取的录像文件信息")
    private List<InHuntingMatchAiFetchedControlRecordInfo> aiFetchedControlRecordsInfos;

    @ApiModelProperty(value = "是否对手（ai）掉线")
    private Boolean isOpponentDisconnect;

    @ApiModelProperty(value = "ai录像选择模式")
    private HuntingMatchAIRecordChooseMode aiRecordChooseMode;

    @ApiModelProperty(value = "匹配AI规则路由表", notes = "如果ai按照回合选择，该值不为null")
    private Integer matchAiRoundRuleTableId;

    @ApiModelProperty(value = "整局匹配中的录像信息", notes = "如果ai按照整局比赛来选择，该值不为null")
    private PlayerUploadWholeMatchControlRecordData wholeMatchControlRecordsData;
}
