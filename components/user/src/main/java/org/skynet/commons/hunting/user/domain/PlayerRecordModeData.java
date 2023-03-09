package org.skynet.commons.hunting.user.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@ApiModel(value = "PlayerRecordModeData对象", description = "玩家录制模式的数据")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PlayerRecordModeData implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("RecordModeMatch表id")
    private Integer recordModeMatchTableId;

    @ApiModelProperty("匹配序列ID")
    private Integer matchSequenceId;
}
