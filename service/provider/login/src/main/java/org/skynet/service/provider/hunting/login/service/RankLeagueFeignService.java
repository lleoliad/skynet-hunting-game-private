package org.skynet.service.provider.hunting.login.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;

@Component
@FeignClient(value = "${skynet.service-list.rank-league.url}/skynet/service/provider/hunting/rank/league")
public interface RankLeagueFeignService {

}
