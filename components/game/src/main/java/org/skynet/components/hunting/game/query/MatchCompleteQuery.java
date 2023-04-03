package org.skynet.components.hunting.game.query;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.skynet.components.hunting.user.dao.entity.UserData;

import java.io.Serializable;
import java.util.List;

@ApiModel(value="MatchCompleteQuery 对象", description="")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@JsonIgnoreProperties(value = {"hibernateLazyInitializer", "handler"})
public class MatchCompleteQuery implements Serializable {

    @ApiModelProperty(value = "版本号")
    private String version;

    @ApiModelProperty(value = "用户ID")
    private String userId;

    @ApiModelProperty(value = "胜利")
    private Boolean isWin;

    @ApiModelProperty(value = "章节id")
    private Integer chapterId;

    @ApiModelProperty(value = "matchId")
    private Integer matchId;

    @ApiModelProperty(value = "所有编码控制记录数据")
    private List<String> allEncodedControlRecordsData;

    @ApiModelProperty(value = "当前服务器是否支持录制模式")
    private Boolean recordOnlyMode;

    @ApiModelProperty(value = "玩家数据")
    private UserData userData;

}
