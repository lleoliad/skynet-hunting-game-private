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
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "PlayerRecordModeData对象", description = "玩家录制模式的数据")
public class PlayerRecordModeData implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("RecordModeMatch表id")
    private Integer recordModeMatchTableId;

    @ApiModelProperty("匹配序列ID")
    private Integer matchSequenceId;
}
