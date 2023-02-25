package org.skynet.service.provider.hunting.obsolete.pojo.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.msgpack.annotation.Message;

import java.io.Serializable;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "ControlSegmentRecordData对象", description = "所有的操作数据")
@Message
public class ControlSegmentRecordData implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "命中火星控制数据")
    private HitFireAtomControlData HitFireAtomControlData;

    private List DraggingControlDataList;

    private StartAimAtomControlData startAimAtomControlData;

    private NotHitFireAtomControlData NotHitFireAtomControlData;

    private ReleaseAimAtomControlData ReleaseAimAtomControlData;

}
