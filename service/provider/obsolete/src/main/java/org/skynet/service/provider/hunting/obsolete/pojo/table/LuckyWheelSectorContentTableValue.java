package org.skynet.service.provider.hunting.obsolete.pojo.table;

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
@ApiModel(value = "LuckyWheelSectorContentTableValue对象", description = "幸运转盘奖励表")
public class LuckyWheelSectorContentTableValue implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("章节id")
    private Integer id;

    @ApiModelProperty("分区宝箱类型")
    private List<Integer> chestTypes;

    @ApiModelProperty("分区金币数量")
    private List<Integer> coinAmounts;
}
