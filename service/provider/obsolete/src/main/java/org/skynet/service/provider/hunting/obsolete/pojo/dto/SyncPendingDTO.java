package org.skynet.service.provider.hunting.obsolete.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "SyncPendingDTO对象", description = "同步待定购买产品")
public class SyncPendingDTO extends BaseDTO {

    @ApiModelProperty("未购买产品")
    private List<String> pendingPurchaseProductsNames;
}
