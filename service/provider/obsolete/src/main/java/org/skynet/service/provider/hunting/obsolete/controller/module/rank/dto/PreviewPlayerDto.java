package org.skynet.service.provider.hunting.obsolete.controller.module.rank.dto;


import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PreviewPlayerDto {


    @ApiModelProperty(value = "用户ID")
    private String userId;

    @ApiModelProperty(value = "版本号")
    private String version;

    @ApiModelProperty(value = "联赛分组ID")
    private String groupId;


}
