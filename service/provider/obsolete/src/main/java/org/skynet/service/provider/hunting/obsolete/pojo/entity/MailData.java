package org.skynet.service.provider.hunting.obsolete.pojo.entity;

import org.skynet.service.provider.hunting.obsolete.enums.MailAttachmentType;
import org.skynet.service.provider.hunting.obsolete.enums.MailType;
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
@ApiModel(value = "MailData对象", description = "邮件对象")
public class MailData implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("邮件唯一id")
    private String uid;

    @ApiModelProperty("邮件类型")
    private MailType mailType;

    @ApiModelProperty("标题")
    private String title;

    @ApiModelProperty("发件人")
    private String senderName;

    @ApiModelProperty("接收时间")
    private Long receiveTime;

    @ApiModelProperty("正文")
    private String message;

    @ApiModelProperty("附件类型")
    private MailAttachmentType attachmentType;

    @ApiModelProperty("附件id")
    private Integer attachmentId;

    @ApiModelProperty("附件数量")
    private Integer attachmentCount;

    @ApiModelProperty("如果是宝箱类型邮件，带有宝箱数据")
    private MailChestContent chestContent;
}
