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
@ApiModel(value = "StartAimAtomControlData对象", description = "起始目标火星控制数据")
@AllArgsConstructor
@NoArgsConstructor
@Message
public class StartAimAtomControlData implements Serializable {

    private static final long serialVersionUID = 1L;

    @Transient
    private Float TimeInSegment;

    private Float StartAimTime;

    private Vector AimInAnimalLocalPosition;


}
