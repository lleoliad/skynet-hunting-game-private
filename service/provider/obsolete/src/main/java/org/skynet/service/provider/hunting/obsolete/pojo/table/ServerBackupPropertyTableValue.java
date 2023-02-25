package org.skynet.service.provider.hunting.obsolete.pojo.table;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "ServerBackupPropertyTableValue对象", description = "比赛单位表")
public class ServerBackupPropertyTableValue implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "用户数据备份间隔分钟")
    private Integer backupUserDataMinutesInterval;
    @ApiModelProperty(value = "保留备份的用户数据天数")
    private Integer preserveBackupUserDataDay;


}
