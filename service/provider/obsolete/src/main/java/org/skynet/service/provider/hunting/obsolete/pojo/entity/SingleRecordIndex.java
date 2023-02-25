package org.skynet.service.provider.hunting.obsolete.pojo.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "SingleRecordIndex对象", description = "单录像索引")
public class SingleRecordIndex implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("整回合索引")
    private String wholeKey;

    @ApiModelProperty("第几局")
    private Integer round;
}
