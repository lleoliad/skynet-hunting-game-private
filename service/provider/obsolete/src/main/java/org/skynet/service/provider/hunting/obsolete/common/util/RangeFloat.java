package org.skynet.service.provider.hunting.obsolete.common.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 时间范围
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RangeFloat implements Serializable {

    private static final long serialVersionUID = 1L;

    private Double _min;
    private Double _max;

    public Double random() {

        return (int) (Math.random() * (this._max - this._min)) + this._min;
    }

    public Boolean contain(Integer value, Integer tolerance) {

        if (tolerance == null)
            tolerance = 0;
        return value >= this._min - tolerance && value <= this._max + tolerance;
    }


}
