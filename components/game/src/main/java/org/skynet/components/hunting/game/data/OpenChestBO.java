package org.skynet.components.hunting.game.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.skynet.components.hunting.user.data.ClientUserData;

import java.io.Serializable;

@ApiModel(value="OpenChestBO 对象", description="BO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(value = {"hibernateLazyInitializer", "handler"})
public class OpenChestBO implements Serializable {

    @ApiModelProperty(value = "用户数据", notes = "")
    private ClientUserData userData;

    private ChestOpenResult chestOpenResult;
}
