package org.skynet.service.provider.hunting.obsolete.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.util.List;
import java.util.Map;

@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "UpdateGUNDTO对象", description = "更新枪支接收对象")
public class UpdateGUNDTO {

    @ApiModelProperty("命令类型")
    private String controlType;

    @ApiModelProperty("需要操作的用户所属服务器")
    private String serverInfo;

    @ApiModelProperty("玩家Uid")
    private String userUid;

    @ApiModelProperty(value = "管理员钥匙")
    private String adminKey;

    @ApiModelProperty("新的枪支等级信息")
    private List<Map<String, Integer>> gunLevelList;
}
