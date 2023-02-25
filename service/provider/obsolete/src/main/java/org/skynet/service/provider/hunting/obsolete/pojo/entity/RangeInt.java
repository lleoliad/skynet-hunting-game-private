package org.skynet.service.provider.hunting.obsolete.pojo.entity;

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
@ApiModel(value = "RangeInt对象", description = "定长数组")
public class RangeInt implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("左边界")
    private Integer min;

    @ApiModelProperty("右边界")
    private Integer max;


    public int random() {
        return (int) Math.round(Math.random() * (this.max - this.min) + this.min);
    }

    public boolean contain(int value) {
        return value >= this.min && value <= this.max;
    }

    public int clampInRange(int value) {
        return Math.min(Math.max(this.min, value), this.max);
    }
}
