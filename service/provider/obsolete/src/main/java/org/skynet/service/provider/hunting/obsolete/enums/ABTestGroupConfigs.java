package org.skynet.service.provider.hunting.obsolete.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * A B test 分布配置
 */

@AllArgsConstructor
@Getter
public enum ABTestGroupConfigs {

    A(ABTestGroup.A, 100, "A组"),
    B(ABTestGroup.B, 0, "B组");

    private final ABTestGroup group;
    private final Integer weight;
    private final String message;

}
