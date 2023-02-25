package org.skynet.service.provider.hunting.obsolete.pojo.entity;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.msgpack.annotation.Message;
import org.springframework.data.annotation.Transient;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "ReleaseAimAtomControlData对象", description = "命中火星控制数据的发布")
@AllArgsConstructor
@NoArgsConstructor
@Message
public class ReleaseAimAtomControlData implements Serializable {

    private static final long serialVersionUID = 1L;

    @Transient
    private float TimeInSegment;

    private float ReleaseTime;
}
