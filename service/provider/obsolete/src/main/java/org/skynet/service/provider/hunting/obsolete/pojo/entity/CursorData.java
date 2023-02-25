package org.skynet.service.provider.hunting.obsolete.pojo.entity;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@ApiModel(value = "CursorData对象", description = "战报数据")
@AllArgsConstructor
@NoArgsConstructor
public class CursorData {

    private Long routeUid;

    private Integer animalId;

    private Integer gunId;

    private Integer gunLevel;

    private Integer bulletId;

    private String docUid;
}
