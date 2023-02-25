package org.skynet.service.provider.hunting.obsolete.pojo.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "MatchRecord对象", description = "匹配池数据，有录像索引和分数")
@AllArgsConstructor
@NoArgsConstructor
public class MatchRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("录像索引")
    private String dataKey;

    @ApiModelProperty("分数")
    private Integer score;

    @ApiModelProperty("玩家Uid")
    private String playerUid;
}
