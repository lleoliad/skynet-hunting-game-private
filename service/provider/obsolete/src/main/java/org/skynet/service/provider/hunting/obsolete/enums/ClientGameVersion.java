package org.skynet.service.provider.hunting.obsolete.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 游戏版本信息
 */
@AllArgsConstructor
@Getter
public enum ClientGameVersion {

    _1_0_0_("_1_0_0", "1.0.0版本"),
    _1_0_1_("_1_0_1", "1.0.1版本"),
    _1_0_2_("_1_0_2", "1.0.2版本"),
    _1_0_3_("_1_0_3", "1.0.3版本"),
    _1_0_4_("_1_0_4", "1.0.4版本"),
    _1_0_5_("_1_0_5", "1.0.5版本"),
    _1_0_6_("_1_0_6", "印度版本"),//印度版本1
    _1_0_7_("_1_0_7", "最新版"),//印度版本1
    _1_0_8_("_1_0_8", "最新版"),//印度版本1
    _1_0_9_("_1_0_9", "最新版"),//印度版本1
    _1_0_10_("_1_0_10", "最新版"),//印度版本1
    _1_0_11_("_1_0_11", "最新版"),//印度版本1
    _1_0_12_("_1_0_12", "最新版"),//印度版本1

    _1_0_13_("_1_0_13", "最新版"),
    _1_0_14_("_1_0_14", "最新版"),//印度版本1
    _1_1_0_("_1_1_0", "最新版"),//1.1.0版本
    ;

    private final String version;
    private final String message;

    public static ClientGameVersion toClientGameVersionEnum(String gameVersion) {


        String clientGameVersionString = "_" + gameVersion.replace(".", "_") + "_";

        return ClientGameVersion.valueOf(clientGameVersionString);
    }


    public static String clientGameVersionEnumToString(ClientGameVersion clientGameVersion) {

        String versionString = clientGameVersion.getVersion();
        versionString = versionString.substring(1, versionString.length());
        versionString = versionString.replace("_", ".");

        return versionString;
    }
}
