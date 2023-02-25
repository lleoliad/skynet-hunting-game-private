package org.skynet.service.provider.hunting.obsolete.pojo.dto;

import org.skynet.service.provider.hunting.obsolete.enums.MailAttachmentType;
import org.skynet.service.provider.hunting.obsolete.enums.MailType;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.MailChestContent;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "SyncPendingDTO对象", description = "同步待定购买产品")
public class MailDataDTO extends BaseDTO {

    @ApiModelProperty("接收人")
    private String targetPlayerUid;

    @ApiModelProperty("邮件类型")
    private MailType mailType;

    @ApiModelProperty("标题")
    private String title;

    @ApiModelProperty("发送人")
    private String senderName;

    @ApiModelProperty("消息")
    private String message;

    @ApiModelProperty("附件类型")
    private MailAttachmentType attachmentType;

    @ApiModelProperty("附件ID")
    private Integer attachmentId;

    @ApiModelProperty("附件内容")
    private Integer attachmentCount;

    @ApiModelProperty("附件头像路径")
    private String attachmentIconPath;

    @ApiModelProperty("如果是宝箱类型邮件，带有宝箱数据")
    private MailChestContent chestContent;

    @ApiModelProperty("邮件Uid")
    private String mailUid;

    @ApiModelProperty("截至的收到时间")
    private Long cursorReceiveTime;

    @ApiModelProperty("上一份邮件Uid")
    private String cursorMailUid;

    @ApiModelProperty("客户端上一次收到的时间")
    private Long clientLatestMailReceiveTime;

    @ApiModelProperty("客户端上一次收到的邮件Uid")
    private String clientLatestMailUid;
}
