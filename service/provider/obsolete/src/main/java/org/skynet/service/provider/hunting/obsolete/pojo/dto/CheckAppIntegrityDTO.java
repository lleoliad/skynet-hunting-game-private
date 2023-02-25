package org.skynet.service.provider.hunting.obsolete.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "CheckAppIntegrityDTO对象", description = "检查APP完整性的接收对象")
public class CheckAppIntegrityDTO extends BaseDTO {

    @ApiModelProperty(value = "哈希码")
    private String codeHash;
}
