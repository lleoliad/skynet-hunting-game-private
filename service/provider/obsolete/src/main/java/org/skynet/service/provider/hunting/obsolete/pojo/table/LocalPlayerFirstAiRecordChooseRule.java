package org.skynet.service.provider.hunting.obsolete.pojo.table;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "LocalPlayerFirstAiRecordChooseRule对象", description = "AI操作选择规则")
public class LocalPlayerFirstAiRecordChooseRule implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "AI操作选择匹配规则集合")
    private Rule[] rules;

    @ApiModelProperty(value = "规则描述")
    private String description;
}
