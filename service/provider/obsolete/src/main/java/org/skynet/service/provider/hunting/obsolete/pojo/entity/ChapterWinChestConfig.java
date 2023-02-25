package org.skynet.service.provider.hunting.obsolete.pojo.entity;

import org.skynet.service.provider.hunting.obsolete.enums.ChestType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@ApiModel(value = "ChapterWinChestConfig对象", description = "章节胜利箱子配置")
public class ChapterWinChestConfig {

    @ApiModelProperty(value = "宝箱槽位数量")
    public static final Integer ChestSlotAmount = 4;

    @ApiModelProperty(value = "木箱解锁时间")
    public static final Integer WoodChestUnlockSeconds = 5;

    @ApiModelProperty(value = "银箱解锁时间")
    public static final Integer SilverChestUnlockSeconds = 4 * 60 * 60;

    @ApiModelProperty(value = "黄金箱解锁时间")
    public static final Integer GoldChestUnlockSeconds = 8 * 60 * 60;

    @ApiModelProperty(value = "白金箱解锁时间")
    public static final Integer PlatinumChestUnlockSeconds = 16 * 60 * 60;

    @ApiModelProperty(value = "king箱子解锁时间")
    public static final Integer KingChestUnlockSeconds = 24 * 60 * 60;

    /**
     * 获取对应箱子的未解锁时间
     *
     * @param chestType
     * @return
     */
    public static Integer getChestUnlockSeconds(ChestType chestType) {
        switch (chestType) {
            case BRONZE:
                return WoodChestUnlockSeconds;
            case SILVER:
                return SilverChestUnlockSeconds;
            case GOLD:
                return GoldChestUnlockSeconds;
            case PLATINUM:
                return PlatinumChestUnlockSeconds;
            case KING:
                return KingChestUnlockSeconds;
        }

        return null;
    }
}
