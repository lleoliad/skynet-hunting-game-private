package org.skynet.commons.hunting.user.service;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.skynet.commons.hunting.user.dao.entity.UserData;
import org.skynet.commons.hunting.user.data.PlayerDetailInfoDTO;
import org.skynet.commons.hunting.user.query.GetPreviewQuery;
import org.skynet.commons.hunting.user.query.UserDataLandQuery;
import org.skynet.commons.hunting.user.query.UserDataUpdateQuery;
import org.skynet.commons.lang.common.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Component
@FeignClient(value = "${skynet.service-list.user.url}/skynet/service/provider/hunting/user")
public interface UserFeignService {
    @PostMapping(value = "player/load")
    Result<UserData> load(@ApiParam(name = "userDataLandQuery", value = "用户数据加载参数", required = true) @RequestBody UserDataLandQuery userDataLandQuery);

    @PostMapping(value = "player/update")
    Result<?> update(@ApiParam(name = "userDataUpdateQuery", value = "用户数据更新参数", required = true) @RequestBody UserDataUpdateQuery userDataUpdateQuery);

    @PostMapping(value = "player/preview")
    Result<PlayerDetailInfoDTO> preview(@ApiParam(name = "getPreviewQuery", value = "获取预览信息参数", required = true) @RequestBody GetPreviewQuery getPreviewQuery);

}
