package org.skynet.service.provider.hunting.login.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;

@Component
@FeignClient(value = "${skynet.service-list.championship.url}/skynet/service/provider/hunting/championship")
public interface ChampionshipFeignService {

}
