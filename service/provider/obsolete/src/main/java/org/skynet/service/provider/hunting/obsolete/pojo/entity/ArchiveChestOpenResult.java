package org.skynet.service.provider.hunting.obsolete.pojo.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.skynet.components.hunting.game.data.ChestOpenResult;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "ArchiveChestOpenResult对象", description = "保存到数据库的开箱奖励对象")
public class ArchiveChestOpenResult implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "用户更新次数")
    private Integer userUpdateCount;

    @ApiModelProperty(value = "开箱结果")
    private ChestOpenResult openResult;
}
