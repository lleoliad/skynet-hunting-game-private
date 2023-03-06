package org.skynet.service.provider.hunting.obsolete.service;

import com.alibaba.fastjson2.JSONObject;
import io.swagger.annotations.ApiParam;
import org.skynet.commons.lang.common.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Component
@FeignClient(value = "${skynet.service-list.rank-league.url}/skynet/service/provider/hunting/rank/league")
public interface RankFeignService {

    @PostMapping(value = "player/login")
    Result<?> login(@ApiParam(name = "loginQuery", value = "登录参数", required = true) @RequestBody JSONObject loginQuery);

}
