package org.skynet.service.provider.hunting.obsolete.pojo.entity;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "ArchivedMailData对象", description = "邮件存档数据")
public class ArchivedMailData implements Serializable {

    private static final long serialVersionUID = 1L;

    private MailData mailData;

    private Long deleteTime;
}
