package org.skynet.service.provider.hunting.obsolete.pojo.bo;

import org.skynet.service.provider.hunting.obsolete.pojo.entity.PlayerControlRecordData;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "RecordDataAndBase64对象", description = "暂存最终查找出的录像信息与压缩编码")
public class RecordDataAndBase64 {

    @ApiModelProperty("查找出来的录像信息")
    private PlayerControlRecordData recordData;

    @ApiModelProperty("压缩的录像文件")
    private String base64;

}
