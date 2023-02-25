package org.skynet.service.provider.hunting.obsolete.pojo.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "DownloadPlayersIconDto对象", description = "获取其他玩家头像数据")
@EqualsAndHashCode(callSuper = false)
public class DownloadPlayersIconDto extends BaseDTO {

    @ApiModelProperty("客户端要拉取的用户 uid 的集合")
    private List<String> downloadIconPlayerIds;

}
