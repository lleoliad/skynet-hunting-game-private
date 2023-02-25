package org.skynet.service.provider.hunting.obsolete.pojo.entity;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.msgpack.annotation.Message;
import org.springframework.data.annotation.Transient;

import java.io.Serializable;
import java.util.Vector;

@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "NotHitFireAtomControlData对象", description = "没有命中火星控制数据")
@Message
@AllArgsConstructor
@NoArgsConstructor
public class NotHitFireAtomControlData implements Serializable {

    private static final long serialVersionUID = 1L;

    @Transient
    private Float TimeInSegment;

    private Float FireTime;

    private Vector AimInAnimalLocalPosition;

    private Boolean IsFlickFire;
}
