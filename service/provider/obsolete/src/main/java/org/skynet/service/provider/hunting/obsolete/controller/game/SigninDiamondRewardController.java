//package com.cn.huntingrivalserver.controller;
//
//import com.cn.huntingrivalserver.common.exception.BusinessException;
//import com.cn.huntingrivalserver.common.util.CommonUtils;
//import com.cn.huntingrivalserver.common.util.TimeUtils;
//import com.cn.huntingrivalserver.config.SystemPropertiesConfig;
//import com.cn.huntingrivalserver.pojo.bo.DiamondRewardBO;
//import com.cn.huntingrivalserver.pojo.dto.BaseDTO;
//import com.cn.huntingrivalserver.pojo.entity.UserData;
//import com.cn.huntingrivalserver.pojo.entity.UserDataSendToClient;
//import com.cn.huntingrivalserver.pojo.environment.GameEnvironment;
//import com.cn.huntingrivalserver.service.SigninDiamondRewardTableService;
//import com.cn.huntingrivalserver.service.UserDataService;
//import io.swagger.annotations.Api;
//import io.swagger.annotations.ApiOperation;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import javax.annotation.Resource;
//import java.util.Map;
//
//@Api(tags = "钻石奖励")
//@RestController
//@RequestMapping("/huntingrival/diamond")
//@Slf4j
//public class SigninDiamondRewardController {
//
//    @Resource
//    private UserDataService userDataService;
//
//    @Resource
//    private SystemPropertiesConfig systemPropertiesConfig;
//
//    @Resource
//    private SigninDiamondRewardTableService signinDiamondRewardTableService;
//
//    @GetMapping("get")
//    @ApiOperation("获得签到钻石奖励")
//    public Map<String,Object> getSigninDiamondReward(@RequestBody BaseDTO dto){
//        try {
//            CommonUtils.requestProcess(dto, null);
//
//            UserDataSendToClient sendToClientData = GameEnvironment.prepareSendToClientUserData();
//            UserData userData = null;
//
//            //处理userData
//            userDataService.checkUserDataExist(dto.getUserUid());
//            userData = GameEnvironment.userDataMap.get(dto.getUserUid());
//            if (!systemPropertiesConfig.getHaveSigninDiamondReward()){
//                throw new BusinessException("当前服务器不支持签到获得钻石");
//            }
//
//            DiamondRewardBO signinDiamondRewardInfosAsync = signinDiamondRewardTableService.getSigninDiamondRewardInfosAsync(userData.getUuid());
//
//            boolean canCollect = signinDiamondRewardInfosAsync.getCanCollect();
//            int diamondReward = signinDiamondRewardInfosAsync.getDiamondReward();
//            if (!canCollect){
//                throw new BusinessException("时间没到,无法获得签到钻石奖励");
//            }
//
//            double tempDiamond = userData.getDiamond();
//            tempDiamond += diamondReward;
//            userData.setDiamond(tempDiamond);
//
//            int tempCount = userData.getServerOnly().getSigninDiamondRewardCollectTimes();
//            userData.getServerOnly().setSigninDiamondRewardCollectTimes(tempCount+1);
//            userData.getServerOnly().setLastSigninDiamondRewardCollectTime(TimeUtils.getUnixTimeSecond());
//
//            sendToClientData.setDiamond(userData.getDiamond());
//            log.info("获取的签到钻石奖励"+diamondReward+".领取次数"+userData.getServerOnly().getSigninDiamondRewardCollectTimes()+",领取时间"+userData.getServerOnly().getLastSigninDiamondRewardCollectTime());
//
//            userDataService.userDataSettlement(userData, sendToClientData);
//            Map<String, Object> map = CommonUtils.responsePrepare(null);
//            map.put("userData",sendToClientData);
//
//
//            return map;
//        }catch (Exception e){
//            CommonUtils.responseException(dto,e.toString());
//        }
//        return null;
//    }
//}
