package org.skynet.service.provider.hunting.obsolete.pojo.dto;

import io.swagger.annotations.ApiModel;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "VipDTO对象", description = "处理VIP数据接收对象")
public class VipDTO extends BaseDTO {

    private Boolean claimVip;

    private Boolean claimSVip;
}
