package org.skynet.service.provider.hunting.login.query;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.io.Serializable;

@ApiModel(value="ChampionshipStartQuery 对象", description="")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@JsonIgnoreProperties(value = {"hibernateLazyInitializer", "handler"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BattleUserCreateQuery implements Serializable {

    @ApiModelProperty(value = "userId")
    @JsonProperty(value = "UID")
    private String userId;

    /**
     * UID      string                 `form:"uid" json:"uid" binding:"required"`
     * 	UserInfo map[string]interface{} `bson:"userInfo,omitempty" json:"userInfo,omitempty" binding:"required"`
     */

}
