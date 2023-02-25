package org.skynet.service.provider.hunting.obsolete.pojo.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.msgpack.annotation.Message;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "ServerResponseArchiveData对象", description = "服务器对客户端的回复存档消息")
@AllArgsConstructor
@NoArgsConstructor
@Message
public class ServerResponseArchiveData implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "函数名")
    private String functionName;

    @ApiModelProperty(value = "请求id")
    private Integer requestId;

    @ApiModelProperty(value = "返回时间")
    private Long responseTime;

    @ApiModelProperty(value = "返回数据")
    private Object response;
}
