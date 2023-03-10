package org.skynet.service.provider.hunting.login.service.impl;

import cn.hutool.core.bean.BeanUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.skynet.commons.lang.common.Result;
import org.skynet.commons.lang.common.SkynetObject;
import org.skynet.components.hunting.battle.query.BattleUserCreateQuery;
import org.skynet.components.hunting.battle.query.OnlineQuery;
import org.skynet.components.hunting.battle.service.BattleFeignService;
import org.skynet.components.hunting.championship.service.ChampionshipFeignService;
import org.skynet.components.hunting.rank.league.query.PlayerLoginQuery;
import org.skynet.components.hunting.rank.league.service.RankLeagueFeignService;
import org.skynet.components.hunting.user.dao.entity.UserData;
import org.skynet.components.hunting.user.data.ClientUserData;
import org.skynet.components.hunting.user.domain.PlayerVipV3Data;
import org.skynet.components.hunting.user.enums.ABTestGroup;
import org.skynet.service.provider.hunting.login.data.LoginVO;
import org.skynet.service.provider.hunting.login.query.LoginQuery;
import org.skynet.service.provider.hunting.login.service.LoginService;
import org.skynet.service.provider.hunting.obsolete.DBOperation.RedisDBOperation;
import org.skynet.service.provider.hunting.obsolete.common.util.CommonUtils;
import org.skynet.service.provider.hunting.obsolete.common.util.TimeUtils;
import org.skynet.service.provider.hunting.obsolete.common.util.thread.ThreadLocalUtil;
import org.skynet.service.provider.hunting.obsolete.config.GameConfig;
import org.skynet.service.provider.hunting.obsolete.config.SystemPropertiesConfig;
import org.skynet.service.provider.hunting.obsolete.config.VipV2Config;
import org.skynet.service.provider.hunting.obsolete.enums.ClientGameVersion;
import org.skynet.service.provider.hunting.obsolete.enums.PlatformName;
import org.skynet.service.provider.hunting.obsolete.pojo.bo.InitUserDataBO;
import org.skynet.service.provider.hunting.obsolete.pojo.dto.LoginDTO;
import org.skynet.service.provider.hunting.obsolete.pojo.environment.GameEnvironment;
import org.skynet.service.provider.hunting.obsolete.pojo.table.LuckyWheelV2PropertyTableValue;
import org.skynet.service.provider.hunting.obsolete.service.IAPService;
import org.skynet.service.provider.hunting.obsolete.service.ObsoleteUserDataService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
public class LoginServiceImpl implements LoginService {

    @Resource
    private SystemPropertiesConfig systemPropertiesConfig;

    @Resource
    private ObsoleteUserDataService obsoleteUserDataService;

    @Resource
    private IAPService iapService;

    @Resource
    private RankLeagueFeignService rankLeagueFeignService;

    @Resource
    private BattleFeignService battleFeignService;

    @Resource
    private ChampionshipFeignService championshipFeignService;

    @SneakyThrows
    @Override
    public Result<LoginVO> login(LoginQuery loginQuery) {
        String loginUserUid = null;
        ThreadLocalUtil.set(loginQuery.getServerTimeOffset());

        LoginDTO loginDTO = BeanUtil.copyProperties(loginQuery, LoginDTO.class);

        CommonUtils.requestProcess(loginDTO, false, systemPropertiesConfig.getSupportRecordModeClient());

        //??????????????????????????????
        UserData newUserData = null;
        String privateKey = loginQuery.getPrivateKey();
        boolean isNewUser = false;

        UserData loginUserData = null;
        if (StringUtils.isEmpty(loginQuery.getUserUid())) {
            //?????????
            isNewUser = true;
            newUserData = obsoleteUserDataService.createNewPlayer(loginQuery.getGameVersion());
            privateKey = newUserData.getServerOnly().getPrivateKey();
            loginUserData = newUserData;
            //??????????????????????????????
            GameEnvironment.userDataMap.put(newUserData.getUuid(), newUserData);
        } else {
            obsoleteUserDataService.checkUserDataExist(loginQuery.getUserUid());
            loginUserData = GameEnvironment.userDataMap.get(loginQuery.getUserUid());
        }

        double giftPackagePopUpRecommendPrice = 0;

        loginUserUid = newUserData == null ? loginDTO.getUserUid() : newUserData.getUuid();

        String userToken = null;

        InitUserDataBO initUserDataBO = obsoleteUserDataService.initUserData(loginUserData, privateKey, loginUserUid, loginDTO);

        loginUserData = initUserDataBO.getUserData();

        userToken = initUserDataBO.getToken();

        giftPackagePopUpRecommendPrice = iapService.getGiftPackagePopUpPriceRecommendPrice(loginUserData);

        obsoleteUserDataService.userDataTransaction(loginUserData, false, loginDTO.getGameVersion());
        loginUserData.getServerOnly().setLastLoginClientVersion(loginDTO.getGameVersion());
        GameEnvironment.userDataMap.remove(loginUserUid);

        boolean disableClientHuntingMatchReport = GameConfig.disableClientHuntingMatchReport;

        //???????????????
        ClientGameVersion latestClientGameVersion = GameConfig.latestClientGameVersion_Android;

        LuckyWheelV2PropertyTableValue luckyWheelV2PropertyTable = GameEnvironment.luckyWheelV2PropertyTableMap.get(loginDTO.getGameVersion());

        Long newRequestId = loginUserData.getUpdateCount() + 1000L; //userDataService.getUserMaxRequestIdNow(loginUserUid);

        if (loginDTO.getPlatform().equals(PlatformName.IOS.getPlatform())) {
            latestClientGameVersion = GameConfig.latestClientGameVersion_IOS;

        }

        if (loginUserData.getLuckyWheelV2Data().getNextFreeSpinUnixTime() <= 0) {
            loginUserData.getLuckyWheelV2Data().setNextFreeSpinUnixTime(TimeUtils.getUnixTimeSecond() + 1000);
        }

        loginUserData.getChapterWinChestsData().removeIf(Objects::isNull);

        GameEnvironment.onlineUser.put(loginUserData.getUuid(), new Date());

        boolean updateUserData = false;

        //?????????????????????????????????????????????????????????????????????????????????????????????false
        if (loginUserData.getIsCreateBattleInfo() == null) {
            loginUserData.setIsCreateBattleInfo(false);
        }

        //?????????vip3
        if (loginUserData.getVipV3Data() == null) {
            loginUserData.setVipV3Data(new PlayerVipV3Data(-1L, -1L, -1L, -1L));
            updateUserData = true;
        }

        // ????????????????????????????????????????????????
        if (!loginUserData.getIsCreateBattleInfo()) {
            SkynetObject userInfo = SkynetObject.builder().build();
            Result<?> battleUserCreateResult = battleFeignService.userCreate(BattleUserCreateQuery.builder().userId(loginUserData.getUuid()).userInfo(userInfo).build());
            if (battleUserCreateResult.failed()) {
                return battleUserCreateResult.build();
            }

            loginUserData.setIsCreateBattleInfo(true);
            updateUserData = true;
        }

        Result<?> battleUserOnlineResult = battleFeignService.userOnline(OnlineQuery.builder().userId(loginUserData.getUuid()).build());
        if (battleUserOnlineResult.failed()) {
            return battleUserOnlineResult.build();
        }

        //????????????
        String latestClientVersion = ClientGameVersion.clientGameVersionEnumToString(latestClientGameVersion);
        ABTestGroup resultGroup = loginUserData.getServerOnly().getAbTestGroup();

        ClientUserData clientUserData = BeanUtil.copyProperties(loginUserData, ClientUserData.class);

        Result<?> rankLeagueLoginResult = rankLeagueFeignService.playerInitialize(PlayerLoginQuery.builder()
                .version(loginQuery.getGameVersion())
                .userId(loginUserUid)
                .nickname(loginUserData.getName())
                .headPic(null)
                .coin(0L)
                .build());

        if (rankLeagueLoginResult.failed()) {
            return rankLeagueLoginResult.build();
        }

        // Result<?> championshipLoginResult = championshipFeignService.playerLogin(org.skynet.components.hunting.championship.query.PlayerLoginQuery.builder()
        //         .version(loginQuery.getGameVersion())
        //         .userId(loginUserUid)
        //         .nickname(loginUserData.getName())
        //         .headPic(null)
        //         .trophyCount(loginUserData.getTrophy())
        //         .build());
        //
        // if (championshipLoginResult.failed()) {
        //     return championshipLoginResult.build();
        // }

        if (updateUserData) {
            RedisDBOperation.insertUserData(loginUserData);
        }

        clientUserData.setPlayerRankData(rankLeagueLoginResult.getData());
        // clientUserData.setPlayerChampionshipData(championshipLoginResult.get("playerChampionshipData"));
        // clientUserData.setChampionshipBadgeData(championshipLoginResult.get("championshipBadgeData"));

        LoginVO loginVO = LoginVO.builder()
                .userData(clientUserData)
                .userToken(userToken)
                .requestId(newRequestId)
                .abTestGroup(resultGroup.getStatus())
                .disableClientHuntingMatchReport(disableClientHuntingMatchReport)
                .latestClientGameVersion(latestClientVersion)
                .standardTimeOffset(GameConfig.standardTimeZoneOffset)
                .vipV2FunctionUnlockDay(VipV2Config.unlockVipFunctionAfterSignUpDayCount)
                .luckyWheelV2FunctionUnlockDay(luckyWheelV2PropertyTable.getFunctionEnableDayFromSignUp())
                .giftPackagePopUpRecommendPrice(giftPackagePopUpRecommendPrice)
                .serverTime(TimeUtils.getUnixTimeSecond())
                .build();

        if (isNewUser) {
            loginVO.setPrivateKey(privateKey);
        }
        return Result.ok(loginVO);
    }
}
