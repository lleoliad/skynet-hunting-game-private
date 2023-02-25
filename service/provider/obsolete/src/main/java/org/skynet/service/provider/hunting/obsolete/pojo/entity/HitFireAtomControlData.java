package org.skynet.service.provider.hunting.obsolete.pojo.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "HitFireAtomControlData对象", description = "命中火星控制数据")
@Message
public class HitFireAtomControlData implements Serializable {

    @Transient
    private Float TimeInSegment;

    private Float FireTime;

    private Vector AimInAnimalLocalPosition;

    private Integer HitMeshTriangleStartIndex;

    private Vector HitTriangleBarycentricPosition;

    private Float BulletMoveTime;

    private Vector HitLocalNormalizedPositionInCriticalPartSpace;

    private Boolean IsFlickFire;

    private Float HitDistanceYard;

    private Vector HitDirectionInTriangleSpace;

    @ApiModelProperty(value = "击中关键部位")
    private Boolean IsHitCriticalPart;

    @ApiModelProperty(value = "击中关键部位配置索引")
    private Integer HitCriticalPartConfigIndex;

    @ApiModelProperty(value = "命中精准度")
    private Float HitShowPrecision;

    @ApiModelProperty(value = "伤害")
    private Integer Damage;

}
