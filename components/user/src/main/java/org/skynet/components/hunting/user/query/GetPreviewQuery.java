package org.skynet.components.hunting.user.query;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.io.Serializable;

@ApiModel(value="GetPreviewQuery 对象", description="")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@JsonIgnoreProperties(value = {"hibernateLazyInitializer", "handler"})
public class GetPreviewQuery implements Serializable {

    @ApiModelProperty(value = "版本号")
    private String version;

    @ApiModelProperty(value = "用户ID")
    private String userId;
}
