package org.skynet.service.provider.hunting.obsolete.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "ChangeNameDTO对象", description = "玩家修改名称接收对象")
public class ChangeNameDTO extends BaseDTO {

    @ApiModelProperty("新名字")
    private String newName;
}
