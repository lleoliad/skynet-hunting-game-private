package org.skynet.service.provider.hunting.obsolete.config;

import com.alibaba.fastjson.JSONObject;
import org.skynet.service.provider.hunting.obsolete.DBOperation.RedisDBOperation;
import org.skynet.service.provider.hunting.obsolete.common.util.IpUtil;
import org.skynet.service.provider.hunting.obsolete.idempotence.RepeatSubmit;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@ControllerAdvice
public class EncryptResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @SneakyThrows
    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        // response.getHeaders().set("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        // OutputStream stream = response.getBody();
        // stream.write(buffer);
        // stream.flush();
        // response.flush();

        if (body instanceof Map) {
            String encoding = "UTF-8";
            String requestBody = IOUtils.toString(request.getBody(), encoding);
            JSONObject params = JSONObject.parseObject(requestBody);

            String path = request.getURI().getPath();
            String remoteAddress = IpUtil.getIpAddress(request);

            RepeatSubmit annotation = returnType.getMethod().getAnnotation(RepeatSubmit.class);
            String cacheKey = null;
            if (annotation != null) {
                cacheKey = "REPEAT_SUBMIT:" + params.getString("userUid") + ":" + params.getIntValue("requestId") + "-" + params.getLongValue("clientTime");
            }

            Map jsonObject = (Map) body;
            Object code = jsonObject.get("code");
            if (code instanceof Number && ((Number)code).intValue() < 0) {
                response.flush();
                log.info("处理完请求，返回失败信息 path:{} remoteAddress:{} params:{} result:{}", path, remoteAddress, params, body);
                if (cacheKey != null) {
                    RedisDBOperation.deleteKey(cacheKey);
                }
                return null;
            } else {
                if (cacheKey != null) {
                    RedisDBOperation.setCacheObject(cacheKey, JSONObject.toJSONString(body), annotation.interval(), TimeUnit.MILLISECONDS);
                    log.info("处理完请求，添加消息缓存，返回成功信息 path:{} remoteAddress:{} params:{} result:{}", path, remoteAddress, params, body);
                } else {
                    log.info("处理完请求，返回成功信息 path:{} remoteAddress:{} params:{} result:{}", path, remoteAddress, params, body);
                }
            }
        }

        return body; // buffer; //objectMapper.writeValueAsBytes(buffer);
    }
}
