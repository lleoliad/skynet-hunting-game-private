package org.skynet.service.provider.hunting.obsolete.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@ApiModel(value = "AIControlRecordDataDTO对象", description = "找到合适的AI操作录制文件接收的请求体")
public class AIControlRecordDataDTO extends BaseDTO {

    @ApiModelProperty(value = "私钥")
    private String privateKey;

    @ApiModelProperty(value = "之前服务器是否有未完成内容", notes = "玩家登陆的时候,会集中处理之前没有执行完的所有服务器方法,这时候可能需要特殊处理")
    private Boolean isFastForwarding;

    @ApiModelProperty(value = "章节id")
    private Integer chapterId;
}
