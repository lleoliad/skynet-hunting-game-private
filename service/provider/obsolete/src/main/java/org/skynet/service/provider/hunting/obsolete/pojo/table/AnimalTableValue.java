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
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "AnimalTableValue对象", description = "怪物数据库表")
public class AnimalTableValue implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "尺寸类型", notes = "1:小型 2:中型 3:大型")
    private Integer sizeType;

    @ApiModelProperty(value = "血量")
    private Integer healthPoint;
}
