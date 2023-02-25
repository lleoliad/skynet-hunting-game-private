package org.skynet.service.provider.hunting.obsolete.common.exception;


import org.skynet.service.provider.hunting.obsolete.common.result.R;
import org.skynet.service.provider.hunting.obsolete.common.result.ResponseEnum;
import org.skynet.service.provider.hunting.obsolete.common.util.CommonUtils;
import org.skynet.service.provider.hunting.obsolete.common.util.TimeUtils;
import org.skynet.service.provider.hunting.obsolete.common.util.thread.ThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.stereotype.Component;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.Map;

/**
 * 截取对特定异常返回自定义信息
 */
@Slf4j
@RestControllerAdvice
@Component
public class UnifiedExceptionHandler {


    /**
     * @param e 未定义异常
     * @return
     */
    @ExceptionHandler(value = Exception.class)
    public R handlerException(Exception e) {
        if (ThreadLocalUtil.localVar.get() != null) {
            ThreadLocalUtil.localVar.remove();
        }
        log.error(e.getMessage(), e);
        return R.error();
    }
    /**
     * redis异常 待补充
     * */

    /**
     * @param e 自定义异常
     * @return
     */
    @ExceptionHandler(value = BusinessException.class)
    public Map<String, Object> BusinessException(BusinessException e) {
        if (ThreadLocalUtil.localVar.get() != null) {
            ThreadLocalUtil.localVar.remove();
        }
        log.error(e.getMessage(), e);
        Map<String, Object> errorMap = CommonUtils.responsePrepare(-1);
        Error error = new Error(e.getMessage());
        errorMap.put("error", error);
        return errorMap;
//        return R.error().message(e.getMessage()).code(e.getCode()).serverTime(TimeUtils.getUnixTimeSecond());
    }

    /**
     * @param e Controller上一层相关异常
     * @return
     */
    @ExceptionHandler({
            NoHandlerFoundException.class,
            HttpRequestMethodNotSupportedException.class,
            HttpMediaTypeNotSupportedException.class,
            MissingPathVariableException.class,
            MissingServletRequestParameterException.class,
            TypeMismatchException.class,
            HttpMessageNotReadableException.class,
            HttpMessageNotWritableException.class,
            MethodArgumentNotValidException.class,
            HttpMediaTypeNotAcceptableException.class,
            ServletRequestBindingException.class,
            ConversionNotSupportedException.class,
            MissingServletRequestPartException.class,
            AsyncRequestTimeoutException.class
    })
    public R handleServletException(Exception e) {
        if (ThreadLocalUtil.localVar.get() != null) {
            ThreadLocalUtil.localVar.remove();
        }
        log.error(e.getMessage(), e);
        return R.error().message(ResponseEnum.SERVLET_ERROR.getMessage()).code(ResponseEnum.SERVLET_ERROR.getCode()).serverTime(TimeUtils.getUnixTimeSecond());
    }
}
