package org.skynet.service.provider.hunting.obsolete.pojo.table;

import org.skynet.service.provider.hunting.obsolete.enums.ChestType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "ChapterChestTaskTableValue对象", description = "章节任务数据库表")
public class ChapterChestTaskTableValue {

    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "宝箱类型", notes = "1:木,2:银,3:金,4:白金,5:king")
    private ChestType chestType;

    @ApiModelProperty(value = "需要完成比赛场次")
    private Integer matchCompleteRequires;
}
