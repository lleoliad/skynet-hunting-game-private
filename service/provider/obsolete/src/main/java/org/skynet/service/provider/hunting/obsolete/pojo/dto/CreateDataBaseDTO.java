package org.skynet.service.provider.hunting.obsolete.pojo.dto;


import org.skynet.service.provider.hunting.obsolete.pojo.entity.CursorData;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "CreateDataBaseDTO对象", description = "创建已有所有录像的分布数据库接收对象")
public class CreateDataBaseDTO {

    @ApiModelProperty(value = "游戏版本")
    private String gameVersion;

    @ApiModelProperty(value = "是否全部重写")
    private Boolean recreateAll;

    @ApiModelProperty(value = "战报数据")
    private CursorData cursorData;

    @ApiModelProperty(value = "管理员密钥")
    private String adminKey;
}
