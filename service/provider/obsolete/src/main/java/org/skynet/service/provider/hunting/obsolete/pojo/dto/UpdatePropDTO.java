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
@ApiModel(value = "UpdatePropDTO对象", description = "更新养成数据接收对象")
public class UpdatePropDTO {

    @ApiModelProperty("命令类型")
    private String controlType;

    @ApiModelProperty("需要操作的用户所属服务器")
    private String serverNum;

    @ApiModelProperty("玩家Uid")
    private String userUid;

    @ApiModelProperty(value = "管理员钥匙")
    private String adminKey;

    @ApiModelProperty(value = "金币数量")
    private Long coin;

    @ApiModelProperty(value = "钻石数量")
    private Long diamond;

    @ApiModelProperty(value = "子弹数量")
    private List<Map<String, Integer>> bulletCountMap;

    @ApiModelProperty(value = "枪械的数量")
    private List<Map<String, Integer>> gunCountMap;
}
