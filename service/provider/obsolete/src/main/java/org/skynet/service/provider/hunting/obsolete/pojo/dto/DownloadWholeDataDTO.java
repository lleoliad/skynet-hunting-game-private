package org.skynet.service.provider.hunting.obsolete.pojo.dto;

import io.swagger.annotations.ApiModel;
import lombok.*;

@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "DownloadWholeDataDTO对象", description = "下载录像命令接收对象")
public class DownloadWholeDataDTO {

    private String adminKey;

    private String filePath;

    private int loadLimit;

    private String collectionPath;

    private String cursorDocId;
}
