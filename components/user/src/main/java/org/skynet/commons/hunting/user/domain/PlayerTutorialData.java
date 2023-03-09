package org.skynet.commons.hunting.user.domain;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;


@ApiModel(value = "PlayerTutorialData对象", description = "玩家引导数据")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PlayerTutorialData implements Serializable {

    private static final long serialVersionUID = 1L;

    private Map<String, Boolean> forceTutorialStepStatusMap;
}
