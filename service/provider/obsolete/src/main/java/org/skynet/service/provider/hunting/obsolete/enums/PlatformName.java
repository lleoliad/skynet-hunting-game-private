package org.skynet.service.provider.hunting.obsolete.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 运行平台
 */
@AllArgsConstructor
@Getter
public enum PlatformName {

    UnityEditor("UnityEditor", "Unity平台"),
    Android("Android", "Android平台"),
    IOS("IOS", "IOS平台"),
    ;

    private final String platform;
    private final String message;
}
