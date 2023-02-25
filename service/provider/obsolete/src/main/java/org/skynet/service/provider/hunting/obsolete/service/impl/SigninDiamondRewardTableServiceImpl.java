package org.skynet.service.provider.hunting.obsolete.service.impl;

import org.skynet.service.provider.hunting.obsolete.config.SystemPropertiesConfig;
import org.skynet.service.provider.hunting.obsolete.service.SigninDiamondRewardTableService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class SigninDiamondRewardTableServiceImpl implements SigninDiamondRewardTableService {

    @Resource
    private SystemPropertiesConfig systemPropertiesConfig;

//    /**
//     * 是否可以获得签到钻石奖励
//     * @return DiamondRewardBO
//     */
//    @Override
//    public DiamondRewardBO getSigninDiamondRewardInfosAsync(String uuid) {
//
//        UserData userData = GameEnvironment.userDataMap.get(uuid);
//        long lastSigninDiamondRewardCollectTime = userData.getServerOnly().getLastSigninDiamondRewardCollectTime();
//        Long unixTimeNow = TimeUtils.getUnixTimeSecond();
//
//        Boolean canCollect = unixTimeNow - lastSigninDiamondRewardCollectTime >= GameConfig.DIAMOND_REWARD_INTERVAL_SECONDS;
//        Integer diamondReward = 0;
//
//        if (canCollect){
//
//            Integer collectTimes = userData.getServerOnly().getSigninDiamondRewardCollectTimes();
//
//            Map<String, SigninDiamondRewardTableValue> signinDiamondRewardTable = GameEnvironment.signinDiamondRewardTableMap;
//
//            int lastDiamondReward = 0;
//            boolean foundResult = false;
//            Set<String> keySet = signinDiamondRewardTable.keySet();
//            for (String key : keySet) {
//                int keyNumber = Integer.parseInt(key);
//                //表格是从1开始的
//                if (keyNumber == collectTimes + 1){
//                    foundResult = true;
//                    diamondReward = signinDiamondRewardTable.get(key).getDiamondReward();
//                    break;
//                }
//            }
//
//            if (!foundResult){
//                diamondReward = lastDiamondReward;
//            }
//        }
//
//        return new DiamondRewardBO(canCollect,diamondReward);
//    }
}
