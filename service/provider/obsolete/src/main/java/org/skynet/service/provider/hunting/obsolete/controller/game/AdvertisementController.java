package org.skynet.service.provider.hunting.obsolete.controller.game;

import org.skynet.service.provider.hunting.obsolete.service.UserDataService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Api(tags = "广告相关")
@RestController
@RequestMapping("/huntingrival")
@Slf4j
public class AdvertisementController {

    @Resource
    private UserDataService userDataService;

//    @GetMapping("watch")
//    @ApiOperation("通过观看广告来获得金币")
//    public Map<String,Object> getCoinByWatchAd(@RequestBody BaseDTO dto){
//
//        try {
//            CommonUtils.requestProcess(dto, null);
//
//            UserDataSendToClient sendToClientData = GameEnvironment.prepareSendToClientUserData();
//            UserData userData = null;
//
//            //处理userData
//            userDataService.checkUserDataExist(dto.getUserUid());
//            userData = GameEnvironment.userDataMap.get(dto.getUserUid());
//
//            if (userData.getCoin() > GameConfig.canPlayAdsToGetCoinMaxAmount){
//                throw new BusinessException("玩家"+dto.getUserUid()+"通过广告获取金币,但是金币数量已经够多了. now "+userData.getCoin());
//            }
//
//            double tempCoin = userData.getCoin()+GameConfig.playAdsGetCoinAmount;
//            userData.setCoin(tempCoin);
//            sendToClientData.setCoin(userData.getCoin());
//
//            log.info("通过广告获得金币. now"+userData.getCoin());
//            userDataService.userDataSettlement(userData, sendToClientData);
//            Map<String, Object> map = CommonUtils.responsePrepare(null);
//
//            map.put("userData",sendToClientData);
//
//            return map;
//        }catch (Exception e){
//            CommonUtils.responseException(dto, e.toString());
//        }
//        return null;
//    }
}
