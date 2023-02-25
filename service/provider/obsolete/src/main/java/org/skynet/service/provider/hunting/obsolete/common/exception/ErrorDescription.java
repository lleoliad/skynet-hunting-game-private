package org.skynet.service.provider.hunting.obsolete.common.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 错误消息返回值
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorDescription {

    private Object requestRawData;

    private String message;
}
