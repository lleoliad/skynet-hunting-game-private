package org.skynet.service.provider.hunting.obsolete.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "CheckDBDTO对象", description = "检查数据库信息接收对象")
public class CheckDBDTO extends BaseDTO {

    @ApiModelProperty("匹配池的matchId")
    private Integer matchId;

    @ApiModelProperty("匹配池的奖杯范围")
    private Integer[] TrophySegmentRange;
}
