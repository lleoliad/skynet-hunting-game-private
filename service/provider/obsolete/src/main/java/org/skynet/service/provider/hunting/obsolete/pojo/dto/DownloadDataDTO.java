package org.skynet.service.provider.hunting.obsolete.pojo.dto;

import io.swagger.annotations.ApiModel;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "DownloadDataDTO对象", description = "谷歌云函数下载录像接收对象")
public class DownloadDataDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String cursorData;

    private List downloadData;

    private int code;
}
