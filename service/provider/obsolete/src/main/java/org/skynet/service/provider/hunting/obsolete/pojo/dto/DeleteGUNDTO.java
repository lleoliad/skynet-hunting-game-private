package org.skynet.service.provider.hunting.obsolete.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "DeleteGUNDTO对象", description = "删除枪支接收对象")
public class DeleteGUNDTO {

    @ApiModelProperty("命令类型")
    private String controlType;

    @ApiModelProperty("需要操作的用户所属服务器")
    private String serverNum;

    @ApiModelProperty("玩家Uid")
    private String userUid;

    @ApiModelProperty(value = "管理员钥匙")
    private String adminKey;

    @ApiModelProperty("要删除的枪的id")
    private List<Integer> gunId;
}
