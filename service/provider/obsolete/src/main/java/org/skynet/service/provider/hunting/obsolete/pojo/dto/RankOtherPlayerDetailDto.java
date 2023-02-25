package org.skynet.service.provider.hunting.obsolete.pojo.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "RankOtherPlayerDetailDto对象", description = "用来查看列表中其他玩家的详细信息")
public class RankOtherPlayerDetailDto extends BaseDTO {

    @ApiModelProperty("其他玩家的uid")
    private String otherPlayerUid;

}
