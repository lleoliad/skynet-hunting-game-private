package org.skynet.service.provider.hunting.obsolete.controller.game;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.skynet.service.provider.hunting.obsolete.DBOperation.RedisDBOperation;
import org.skynet.service.provider.hunting.obsolete.common.exception.BusinessException;
import org.skynet.service.provider.hunting.obsolete.common.util.CommonUtils;
import org.skynet.service.provider.hunting.obsolete.common.util.HttpUtil;
import org.skynet.service.provider.hunting.obsolete.common.util.thread.ThreadLocalUtil;
import org.skynet.service.provider.hunting.obsolete.config.SystemPropertiesConfig;
import org.skynet.service.provider.hunting.obsolete.enums.AuthenticationProvider;
import org.skynet.service.provider.hunting.obsolete.idempotence.RepeatSubmit;
import org.skynet.service.provider.hunting.obsolete.pojo.dto.AuthenticationDTO;
import org.skynet.service.provider.hunting.obsolete.pojo.dto.BaseDTO;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.AccountMapData;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.AccountSummaryData;
import org.skynet.components.hunting.user.dao.entity.UserData;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.UserDataSendToClient;
import org.skynet.service.provider.hunting.obsolete.pojo.environment.GameEnvironment;
import org.skynet.service.provider.hunting.obsolete.pojo.vo.DeviceIdUser;
import org.skynet.service.provider.hunting.obsolete.pojo.vo.FacebookIdUser;
import org.skynet.service.provider.hunting.obsolete.pojo.vo.GoogleIdUser;
import org.skynet.service.provider.hunting.obsolete.service.ObsoleteUserDataService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Api(tags = "用户验证")
@RestController
@RequestMapping("/huntingrival")
@Slf4j
public class AuthenticationController {

    @Resource
    private SystemPropertiesConfig systemPropertiesConfig;

    @Resource
    private ObsoleteUserDataService obsoleteUserDataService;


    @PostMapping("auth-getAuthenticationProviderBindGameAccount")
    @ApiOperation("玩家拉取账号信息")
    @RepeatSubmit(interval = 60000)
    public Map<String, Object> getAuthenticationProviderBindGameAccount(@RequestBody AuthenticationDTO request) {

        try {
            String cloudUrl = systemPropertiesConfig.getCloudUrl();
            UserData bindPlayerData;
            CommonUtils.requestProcess(request, null, systemPropertiesConfig.getSupportRecordModeClient());
            obsoleteUserDataService.ensureUserDataIdempotence(request.getUserUid(), request.getUserDataUpdateCount(), request.getGameVersion());
            Map<String, Object> map = CommonUtils.responsePrepare(null);
            AccountSummaryData accountSummaryData = new AccountSummaryData();


            //google账号登录
            if (request.getAuthenticationProvider() == AuthenticationProvider.Google) {
                log.warn("开始验证玩家谷歌账号");
                //验证玩家登陆账号
                obsoleteUserDataService.googleAuthenticationValidate(request.getProviderUserId(), request.getIdToken());
                AccountMapData googleAccountData = RedisDBOperation.selectGoogleAccountMapData(request.getProviderUserId());
                if (googleAccountData == null) {
                    log.warn("本地数据库不存在玩家数据，开始从远程拉取");
                    //https://asia-south1-huntingmasterrecord.cloudfunctions.net/admin-downloadUserData  印度服
                    //https://us-central1-wildhunthuntingclash.cloudfunctions.net/admin-downloadUserData  正式服
                    String url = cloudUrl + "/admin-downloadUserData";
                    GoogleIdUser googleIdUser = new GoogleIdUser("huF9NVzVKRQ^F&Nb8Cgmnhtl#Nvolu", request.getProviderUserId());
                    bindPlayerData = HttpUtil.getUserDataFromCloud(url, googleIdUser);
                    if (bindPlayerData == null) {
                        log.info("当前谷歌账户：{}，未绑定任何游戏账号", request.getProviderUserId());
                        map.put("bindPlayerData", null);
                        return map;
                    }
                    AccountMapData googleAccountMapData = new AccountMapData(request.getProviderUserId(), bindPlayerData.getUuid());
                    RedisDBOperation.insertGoogleAccountMapData(request.getProviderUserId(), googleAccountMapData);

                    if (RedisDBOperation.checkKeyExist("User:" + bindPlayerData.getUuid())) {
                        log.warn("本地仓库中的已经存在玩家数据");
                        bindPlayerData = RedisDBOperation.selectUserData("User:" + bindPlayerData.getUuid());
                    } else {
                        log.warn("将远程仓库中的玩家数据存储到本地");
                        RedisDBOperation.insertUserData(bindPlayerData);
                    }

                    String linkedPlayerUid = bindPlayerData.getUuid();
                    log.info("当前google账号" + request.getProviderUserId() + "对应的玩家账号：" + linkedPlayerUid);
                    log.warn("当前google账号绑定的游戏账号数据：{}", JSONUtil.toJsonStr(bindPlayerData));

                    accountSummaryData.setUid(linkedPlayerUid);
                    accountSummaryData.setPlayerName(bindPlayerData.getName());
                    accountSummaryData.setTrophy(bindPlayerData.getTrophy());
                    map.put("bindPlayerData", accountSummaryData);
                    return map;
                }

                log.warn("本地数据库存在玩家数据,直接从查找");
                String linkedPlayerUid = googleAccountData.getLinkedPlayerUid();
                bindPlayerData = RedisDBOperation.selectUserData("User:" + linkedPlayerUid);
                log.info("当前登录的google账号：" + request.getProviderUserId() + "对应的玩家账号：" + linkedPlayerUid);
                log.warn("当前google账号绑定的游戏账号数据：{}", JSONUtil.toJsonStr(bindPlayerData));

                accountSummaryData.setUid(linkedPlayerUid);
                accountSummaryData.setPlayerName(bindPlayerData.getName());
                accountSummaryData.setTrophy(bindPlayerData.getTrophy());
                map.put("bindPlayerData", accountSummaryData);
                return map;

            } else if (request.getAuthenticationProvider() == AuthenticationProvider.Facebook) {
                log.warn("开始验证玩家Facebook账号");

                obsoleteUserDataService.facebookAuthenticationValidate(request.getProviderUserId(), request.getIdToken());

                AccountMapData facebookAccountData = RedisDBOperation.selectFacebookAccountMapData(request.getProviderUserId());

                if (facebookAccountData == null) {
                    log.warn("本地数据库不存在玩家数据，开始从远程拉取");
                    String url = cloudUrl + "/admin-downloadUserData";
                    FacebookIdUser facebookIdUser = new FacebookIdUser("huF9NVzVKRQ^F&Nb8Cgmnhtl#Nvolu", request.getProviderUserId());
                    bindPlayerData = HttpUtil.getUserDataFromCloud(url, facebookIdUser);
                    if (bindPlayerData == null) {
                        log.info("当前facebook账户：{}，未绑定任何游戏账号", request.getProviderUserId());
                        map.put("bindPlayerData", null);
                        return map;
                    }
                    //将facebookAccountMapData,存入map
                    AccountMapData facebookAccountMapData = new AccountMapData(request.getProviderUserId(), bindPlayerData.getUuid());
                    RedisDBOperation.insertFacebookAccountMapData(request.getProviderUserId(), facebookAccountMapData);

                    if (RedisDBOperation.checkKeyExist("User:" + bindPlayerData.getUuid())) {
                        log.warn("本地仓库中的已经存在玩家数据");
                        bindPlayerData = RedisDBOperation.selectUserData("User:" + bindPlayerData.getUuid());
                    } else {
                        log.warn("将远程仓库中的玩家数据存储到本地");
                        RedisDBOperation.insertUserData(bindPlayerData);
                    }

                    String linkedPlayerUid = bindPlayerData.getUuid();
                    log.info("当前登录的facebook账号：" + request.getProviderUserId() + "对应的玩家账号：" + linkedPlayerUid);
                    log.warn("当前facebook账号绑定的游戏账号数据：{}", JSONUtil.toJsonStr(bindPlayerData));

                    accountSummaryData.setUid(linkedPlayerUid);
                    accountSummaryData.setPlayerName(bindPlayerData.getName());
                    accountSummaryData.setTrophy(bindPlayerData.getTrophy());
                    map.put("bindPlayerData", accountSummaryData);
                    return map;
                }

                log.warn("本地数据库存在玩家数据，直接进行恢复");
                String linkedPlayerUid = facebookAccountData.getLinkedPlayerUid();
                bindPlayerData = RedisDBOperation.selectUserData("User:" + linkedPlayerUid);
                log.info("当前登录的facebook账号：" + request.getProviderUserId() + "对应的玩家账号" + linkedPlayerUid);
                log.warn("当前facebook账号绑定的游戏账号数据：{}", JSONUtil.toJsonStr(bindPlayerData));

                accountSummaryData.setUid(linkedPlayerUid);
                accountSummaryData.setPlayerName(bindPlayerData.getName());
                accountSummaryData.setTrophy(bindPlayerData.getTrophy());
                map.put("bindPlayerData", accountSummaryData);
                return map;
            }

            log.info("登录方式不为google或者facebook");
            return map;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }


    @PostMapping("auth-recoverAuthenticationProviderGameAccount")
    @ApiOperation("恢复第三方鉴权")
    @RepeatSubmit(interval = 60000)
    public Map<String, Object> recoverAuthenticationProviderGameAccount(@RequestBody AuthenticationDTO request) {
        log.warn("进入方法：auth-recoverAuthenticationProviderGameAccount");
        try {
            UserData existPlayerData;
            ThreadLocalUtil.set(request.getServerTimeOffset());
            String cloudUrl = systemPropertiesConfig.getCloudUrl();
            CommonUtils.requestProcess(request, null, systemPropertiesConfig.getSupportRecordModeClient());
            obsoleteUserDataService.ensureUserDataIdempotence(request.getUserUid(), request.getUserDataUpdateCount(), request.getGameVersion());
            UserDataSendToClient userDataSendToClient = GameEnvironment.prepareSendToClientUserData();
            if (request.getAuthenticationProvider() == AuthenticationProvider.Google) {

                log.warn("开始验证玩家谷歌账号");
                //验证玩家登陆账号
                obsoleteUserDataService.googleAuthenticationValidate(request.getProviderUserId(), request.getIdToken());

                AccountMapData googleAccountData = RedisDBOperation.selectGoogleAccountMapData(request.getProviderUserId());

                if (googleAccountData == null) {
                    log.warn("本地数据库不存在玩家数据，开始从远程拉取");
                    String url = cloudUrl + "/admin-downloadUserData";
                    log.info("数据库地址：" + url);
                    GoogleIdUser googleIdUser = new GoogleIdUser("huF9NVzVKRQ^F&Nb8Cgmnhtl#Nvolu", request.getProviderUserId());
                    existPlayerData = HttpUtil.getUserDataFromCloud(url, googleIdUser);
                    if (existPlayerData == null) {
                        throw new BusinessException("不存在google账号登陆记录" + request.getProviderUserId());
                    }
                    AccountMapData googleAccountMapData = new AccountMapData(request.getProviderUserId(), existPlayerData.getUuid());
                    RedisDBOperation.insertGoogleAccountMapData(request.getProviderUserId(), googleAccountMapData);

                    if (RedisDBOperation.checkKeyExist("User:" + existPlayerData.getUuid())) {
                        log.warn("本地仓库中的已经存在玩家数据");
                        existPlayerData = RedisDBOperation.selectUserData("User:" + existPlayerData.getUuid());
                    } else {
                        log.warn("将远程仓库中的玩家数据存储到本地");
                        RedisDBOperation.insertUserData(existPlayerData);
                    }

                    String linkedPlayerUid = existPlayerData.getUuid();
                    log.info("恢复google账号" + request.getProviderUserId() + "对应的玩家账号" + linkedPlayerUid);
                    log.warn("恢复的玩家数据信息为：{}", JSONUtil.toJsonStr(existPlayerData));
                    Map<String, Object> map = CommonUtils.responsePrepare(null);
                    BeanUtils.copyProperties(existPlayerData, userDataSendToClient);
                    obsoleteUserDataService.userDataSettlement(existPlayerData, userDataSendToClient, false, request.getGameVersion());
                    map.put("userData", userDataSendToClient);
                    map.put("privateKey", existPlayerData.getServerOnly().getPrivateKey());

                    // TODO 处理缺省值的错误
                    String sv = JSON.toJSONString(map, SerializerFeature.WriteMapNullValue);
                    String siv = JSON.toJSONString(map);
                    if (sv.length() != siv.length()) {
                        map = JSON.parseObject(siv);
                        log.warn("缺省值异常:{}", siv);
                    }
                    return map;
                }
                log.warn("本地数据库存在玩家数据，直接进行恢复");
                String linkedPlayerUid = googleAccountData.getLinkedPlayerUid();
                existPlayerData = RedisDBOperation.selectUserData("User:" + linkedPlayerUid);
                log.info("恢复google账号" + request.getProviderUserId() + "对应的玩家账号" + linkedPlayerUid);
                log.warn("恢复的玩家数据信息为：{}", JSONUtil.toJsonStr(existPlayerData));
                Map<String, Object> map = CommonUtils.responsePrepare(null);
                BeanUtils.copyProperties(existPlayerData, userDataSendToClient);
                obsoleteUserDataService.userDataSettlement(existPlayerData, userDataSendToClient, false, request.getGameVersion());
                map.put("userData", userDataSendToClient);
                map.put("privateKey", existPlayerData.getServerOnly().getPrivateKey());

                // TODO 处理缺省值的错误
                String sv = JSON.toJSONString(map, SerializerFeature.WriteMapNullValue);
                String siv = JSON.toJSONString(map);
                if (sv.length() != siv.length()) {
                    map = JSON.parseObject(siv);
                    log.warn("缺省值异常:{}", siv);
                }
                return map;
            } else if (request.getAuthenticationProvider() == AuthenticationProvider.Facebook) {
                log.warn("开始验证玩家Facebook账号");

                obsoleteUserDataService.facebookAuthenticationValidate(request.getProviderUserId(), request.getIdToken());

                AccountMapData facebookAccountData = RedisDBOperation.selectFacebookAccountMapData(request.getProviderUserId());


                if (facebookAccountData == null) {
                    log.warn("本地数据库不存在玩家数据，开始从远程拉取");
                    String url = cloudUrl + "/admin-downloadUserData";
                    log.info("数据库地址：" + url);
                    FacebookIdUser facebookIdUser = new FacebookIdUser("huF9NVzVKRQ^F&Nb8Cgmnhtl#Nvolu", request.getProviderUserId());
                    existPlayerData = HttpUtil.getUserDataFromCloud(url, facebookIdUser);
                    if (existPlayerData == null) {
                        throw new BusinessException("不存在FaceBook账号登陆记录" + request.getProviderUserId());
                    }
                    //将facebookAccountMapData,存入map
                    AccountMapData facebookAccountMapData = new AccountMapData(request.getProviderUserId(), existPlayerData.getUuid());
                    RedisDBOperation.insertFacebookAccountMapData(request.getProviderUserId(), facebookAccountMapData);

                    if (RedisDBOperation.checkKeyExist("User:" + existPlayerData.getUuid())) {
                        log.warn("本地仓库中的已经存在玩家数据");
                        existPlayerData = RedisDBOperation.selectUserData("User:" + existPlayerData.getUuid());
                    } else {
                        log.warn("将远程仓库中的玩家数据存储到本地");
                        RedisDBOperation.insertUserData(existPlayerData);
                    }

                    String linkedPlayerUid = existPlayerData.getUuid();
                    log.info("恢复facebook账号" + request.getProviderUserId() + "对应的玩家账号" + linkedPlayerUid);
                    log.warn("恢复的玩家数据信息为：{}", JSONUtil.toJsonStr(existPlayerData));
                    Map<String, Object> map = CommonUtils.responsePrepare(null);
                    BeanUtils.copyProperties(existPlayerData, userDataSendToClient);
                    obsoleteUserDataService.userDataSettlement(existPlayerData, userDataSendToClient, false, request.getGameVersion());
                    map.put("userData", userDataSendToClient);
                    map.put("privateKey", existPlayerData.getServerOnly().getPrivateKey());

                    // TODO 处理缺省值的错误
                    String sv = JSON.toJSONString(map, SerializerFeature.WriteMapNullValue);
                    String siv = JSON.toJSONString(map);
                    if (sv.length() != siv.length()) {
                        map = JSON.parseObject(siv);
                        log.warn("缺省值异常:{}", siv);
                    }
                    return map;
                }

                log.warn("本地数据库存在玩家数据，直接进行恢复");
                String linkedPlayerUid = facebookAccountData.getLinkedPlayerUid();
                existPlayerData = RedisDBOperation.selectUserData("User:" + linkedPlayerUid);
                log.info("恢复facebook账号" + request.getProviderUserId() + "对应的玩家账号" + linkedPlayerUid);
                log.warn("恢复的玩家数据信息为：{}", JSONUtil.toJsonStr(existPlayerData));
                Map<String, Object> map = CommonUtils.responsePrepare(null);
                BeanUtils.copyProperties(existPlayerData, userDataSendToClient);
                obsoleteUserDataService.userDataSettlement(existPlayerData, userDataSendToClient, false, request.getGameVersion());
                map.put("userData", userDataSendToClient);
                map.put("privateKey", existPlayerData.getServerOnly().getPrivateKey());

                // TODO 处理缺省值的错误
                String sv = JSON.toJSONString(map, SerializerFeature.WriteMapNullValue);
                String siv = JSON.toJSONString(map);
                if (sv.length() != siv.length()) {
                    map = JSON.parseObject(siv);
                    log.warn("缺省值异常:{}", siv);
                }
                return map;
            }

        } catch (Exception e) {
            CommonUtils.responseException(request, e, request.getUserUid());
        } finally {
            ThreadLocalUtil.remove();
        }

        return null;
    }


    @PostMapping("auth-authenticationLogin")
    @ApiOperation("玩家通过第三方鉴权登陆")
    @RepeatSubmit(interval = 60000)
    public Map<String, Object> authenticationLogin(@RequestBody AuthenticationDTO request) {

        try {
            ThreadLocalUtil.set(request.getServerTimeOffset());
            String cloudUrl = systemPropertiesConfig.getCloudUrl();
            CommonUtils.requestProcess(request, null, systemPropertiesConfig.getSupportRecordModeClient());
            obsoleteUserDataService.ensureUserDataIdempotence(request.getUserUid(), request.getUserDataUpdateCount(), request.getGameVersion());
            UserDataSendToClient userDataSendToClient = GameEnvironment.prepareSendToClientUserData();


            UserData existPlayerData = RedisDBOperation.selectUserData("User:" + request.getUserUid());

            if (existPlayerData == null) {
                throw new BusinessException("玩家数据为空");
            }

            if (request.getAuthenticationProvider().equals(AuthenticationProvider.Google)) {
                log.info("google鉴权登陆");

                GoogleIdToken.Payload payload = obsoleteUserDataService.googleAuthenticationValidate(request.getProviderUserId(), request.getIdToken());

                //检查该账号是否有游戏账号
                AccountMapData googleAccountMapData = RedisDBOperation.selectGoogleAccountMapData(request.getProviderUserId());

                if (googleAccountMapData == null) {
                    log.warn("本地数据库不存在玩家数据，开始从远程拉取");
                    String url = cloudUrl + "/admin-downloadUserData";
                    log.info("数据库地址：" + url);
                    GoogleIdUser googleIdUser = new GoogleIdUser("huF9NVzVKRQ^F&Nb8Cgmnhtl#Nvolu", request.getProviderUserId());
                    UserData userDataFromCloud = HttpUtil.getUserDataFromCloud(url, googleIdUser);
                    if (userDataFromCloud != null) {
                        existPlayerData = userDataFromCloud;
                        log.info("google登陆鉴权,账号" + request.getProviderUserId() + "已经存在游戏账号了.");
                        googleAccountMapData = new AccountMapData(request.getProviderUserId(), userDataFromCloud.getUuid());
                        RedisDBOperation.insertGoogleAccountMapData(request.getProviderUserId(), googleAccountMapData);

                        if (RedisDBOperation.checkKeyExist("User:" + existPlayerData.getUuid())) {
                            log.warn("本地仓库中的已经存在玩家数据");
                            existPlayerData = RedisDBOperation.selectUserData("User:" + existPlayerData.getUuid());
                        } else {
                            log.warn("将远程数据库中的玩家数据存入本地");
                            RedisDBOperation.insertUserData(existPlayerData);
                        }

                        Map<String, Object> map = CommonUtils.responsePrepare(null);
                        Map<String, Object> alreadyExistPlayerData = new HashMap<>();
                        alreadyExistPlayerData.put("uid", googleAccountMapData.getLinkedPlayerUid());
                        alreadyExistPlayerData.put("playerName", existPlayerData.getName());
                        alreadyExistPlayerData.put("trophy", existPlayerData.getTrophy());
                        map.put("alreadyExistPlayerData", alreadyExistPlayerData);
                        return map;
                    }
                    log.warn("远程数据库中也不存在改玩家数据");
                }


                if (googleAccountMapData != null) {
                    log.warn("本地数据库存在玩家数据,{}", JSONUtil.toJsonStr(googleAccountMapData));
                    if (!googleAccountMapData.getLinkedPlayerUid().equals(request.getUserUid())) {
                        existPlayerData = RedisDBOperation.selectUserData("User:" + googleAccountMapData.getLinkedPlayerUid());
                        log.info("google登陆鉴权,账号" + request.getProviderUserId() + "已经存在游戏账号了.");

                        Map<String, Object> map = CommonUtils.responsePrepare(null);
                        Map<String, Object> alreadyExistPlayerData = new HashMap<>();
                        alreadyExistPlayerData.put("uid", googleAccountMapData.getLinkedPlayerUid());
                        alreadyExistPlayerData.put("playerName", existPlayerData.getName());
                        alreadyExistPlayerData.put("trophy", existPlayerData.getTrophy());

                        map.put("alreadyExistPlayerData", alreadyExistPlayerData);
                        return map;
                    }
                }

                //存入map
                googleAccountMapData = new AccountMapData(request.getProviderUserId(), request.getUserUid());
                RedisDBOperation.insertGoogleAccountMapData(request.getProviderUserId(), googleAccountMapData);

                String pictureUrl = (String) payload.get("picture");
                //保存头像
                obsoleteUserDataService.saveUserProfileImage(pictureUrl, request.getUserUid());

                //正常登录，绑定该账号
                if (StringUtils.isEmpty(existPlayerData.getLinkedAuthProviderData().getGooglePlayUserId())) {

                    existPlayerData.getLinkedAuthProviderData().setGooglePlayUserId(request.getProviderUserId());
                    userDataSendToClient.setLinkedAuthProviderData(existPlayerData.getLinkedAuthProviderData());
                }

                //这里是否会传deviceId
                if (request.getDeviceId() != null) {
                    obsoleteUserDataService.deleteGuestAccountData(existPlayerData, request.getDeviceId());
                }
                obsoleteUserDataService.userDataSettlement(existPlayerData, userDataSendToClient, true, request.getGameVersion());

                Map<String, Object> map = CommonUtils.responsePrepare(null);
                map.put("userData", userDataSendToClient);
                return map;
            } else if (request.getAuthenticationProvider() == AuthenticationProvider.Facebook) {

                log.info("facebook 鉴权登录");

                String serverToken = obsoleteUserDataService.facebookAuthenticationValidate(request.getProviderUserId(), request.getIdToken());
                log.info("facebook 鉴权登陆验证成功");

                //检查该账号是否有游戏账号
                AccountMapData facebookAccountMapData = RedisDBOperation.selectFacebookAccountMapData(request.getProviderUserId());

                if (facebookAccountMapData == null) {
                    log.warn("本地数据库不存在玩家数据，开始从远程拉取");
                    String url = cloudUrl + "/admin-downloadUserData";
                    FacebookIdUser facebookIdUser = new FacebookIdUser("huF9NVzVKRQ^F&Nb8Cgmnhtl#Nvolu", request.getProviderUserId());
                    UserData userDataFromCloud = HttpUtil.getUserDataFromCloud(url, facebookIdUser);
                    if (userDataFromCloud != null) {
                        existPlayerData = userDataFromCloud;
                        log.info("facebook 登陆鉴权,账号" + request.getProviderUserId() + "已经存在游戏账号了.");
                        //将facebookAccountMapData,存入map
                        facebookAccountMapData = new AccountMapData(request.getProviderUserId(), userDataFromCloud.getUuid());
                        RedisDBOperation.insertFacebookAccountMapData(request.getProviderUserId(), facebookAccountMapData);

                        if (RedisDBOperation.checkKeyExist("User:" + userDataFromCloud.getUuid())) {
                            log.warn("本地仓库中的已经存在玩家数据");
                            existPlayerData = RedisDBOperation.selectUserData("User:" + existPlayerData.getUuid());
                        } else {
                            log.warn("将远程数据库中的玩家数据存入本地");
                            RedisDBOperation.insertUserData(userDataFromCloud);
                        }

                        Map<String, Object> map = CommonUtils.responsePrepare(null);
                        Map<String, Object> alreadyExistPlayerData = new HashMap<>();
                        alreadyExistPlayerData.put("uid", facebookAccountMapData.getLinkedPlayerUid());
                        alreadyExistPlayerData.put("playerName", existPlayerData.getName());
                        alreadyExistPlayerData.put("trophy", existPlayerData.getTrophy());

                        map.put("alreadyExistPlayerData", alreadyExistPlayerData);
                        return map;
                    }
                    log.warn("远程数据库中也不存在改玩家数据");
                }

                if (facebookAccountMapData != null) {

                    if (!facebookAccountMapData.getLinkedPlayerUid().equals(request.getUserUid())) {
                        existPlayerData = RedisDBOperation.selectUserData("User:" + facebookAccountMapData.getLinkedPlayerUid());
                        log.info("facebook 登陆鉴权,账号" + request.getProviderUserId() + "已经存在游戏账号了.");
                        Map<String, Object> map = CommonUtils.responsePrepare(null);
                        Map<String, Object> alreadyExistPlayerData = new HashMap<>();
                        alreadyExistPlayerData.put("uid", facebookAccountMapData.getLinkedPlayerUid());
                        alreadyExistPlayerData.put("playerName", existPlayerData.getName());
                        alreadyExistPlayerData.put("trophy", existPlayerData.getTrophy());

                        map.put("alreadyExistPlayerData", alreadyExistPlayerData);
                        return map;
                    }
                }

                //第一次鉴权登陆,存入map
                facebookAccountMapData = new AccountMapData(request.getProviderUserId(), request.getUserUid());
                RedisDBOperation.insertFacebookAccountMapData(request.getProviderUserId(), facebookAccountMapData);

                //保存头像
                String profileImageUrl = "https://graph.facebook.com/v14.0/" + request.getProviderUserId() + "/picture?type=square&access_token=" + serverToken;
                obsoleteUserDataService.saveUserProfileImage(profileImageUrl, request.getUserUid());

                //正常登录，绑定该账号
                if (StringUtils.isEmpty(existPlayerData.getLinkedAuthProviderData().getFacebookUserId())) {

                    existPlayerData.getLinkedAuthProviderData().setFacebookUserId(request.getProviderUserId());
                    userDataSendToClient.setLinkedAuthProviderData(existPlayerData.getLinkedAuthProviderData());
                }

                //这里是否会传deviceId
                if (request.getDeviceId() != null) {
                    obsoleteUserDataService.deleteGuestAccountData(existPlayerData, request.getDeviceId());
                }
                obsoleteUserDataService.userDataSettlement(existPlayerData, userDataSendToClient, true, request.getGameVersion());

                Map<String, Object> map = CommonUtils.responsePrepare(null);
                map.put("userData", userDataSendToClient);
                return map;
            }

            throw new BusinessException("不支持的鉴权平台:" + request.getAuthenticationProvider());

        } catch (Exception e) {
            CommonUtils.responseException(request, e, request.getUserUid());
        } finally {
            ThreadLocalUtil.remove();
        }
        return null;
    }

    @PostMapping("auth-recoverGuestGameAccount")
    @ApiOperation("恢复游客账号")
    @RepeatSubmit(interval = 60000)
    public Map<String, Object> recoverGuestGameAccount(@RequestBody BaseDTO request) {

        try {
            ThreadLocalUtil.set(request.getServerTimeOffset());
            String cloudUrl = systemPropertiesConfig.getCloudUrl();
            CommonUtils.requestProcess(request, null, systemPropertiesConfig.getSupportRecordModeClient());
            obsoleteUserDataService.ensureUserDataIdempotence(request.getUserUid(), request.getUserDataUpdateCount(), request.getGameVersion());

            AccountMapData accountMapData = RedisDBOperation.selectGuestAccount(request.getDeviceId(), request.getUserUid());

            if (accountMapData == null) {
                String url = cloudUrl + "/admin-downloadUserData";
                DeviceIdUser deviceIdUser = new DeviceIdUser("huF9NVzVKRQ^F&Nb8Cgmnhtl#Nvolu", request.getDeviceId());
                UserData userDataFromDevice = HttpUtil.getUserDataFromCloud(url, deviceIdUser);
                if (userDataFromDevice == null) {
                    throw new BusinessException("没有device id" + request.getDeviceId() + "绑定的游客账号");
                }
                Map<String, Object> map = CommonUtils.responsePrepare(null);
                map.put("userData", userDataFromDevice);
                map.put("privateKey", userDataFromDevice.getServerOnly().getPrivateKey());
                return map;
            }

            String linkedPlayerUid = accountMapData.getLinkedPlayerUid();
            UserData existPlayerData = RedisDBOperation.selectUserData("User:" + linkedPlayerUid);
            log.info("恢复device id" + request.getDeviceId() + "绑定的游客账号" + JSONObject.toJSONString(existPlayerData));

            Map<String, Object> map = CommonUtils.responsePrepare(null);
            map.put("userData", existPlayerData);
            map.put("privateKey", existPlayerData.getServerOnly().getPrivateKey());
            return map;

        } catch (Exception e) {
            CommonUtils.responseException(request, e, request.getUserUid());
        } finally {
            ThreadLocalUtil.remove();
        }
        return null;
    }

    @PostMapping("auth-findBindGuestGameAccount")
    @ApiOperation("查找某个device id是否绑定了游戏账号")
    @RepeatSubmit(interval = 60000)
    public Map<String, Object> findBindGuestGameAccount(@RequestBody BaseDTO request) {

        try {
            Map<String, Object> map = CommonUtils.responsePrepare(null);
            ThreadLocalUtil.set(request.getServerTimeOffset());
            String cloudUrl = systemPropertiesConfig.getCloudUrl();
            CommonUtils.requestProcess(request, null, systemPropertiesConfig.getSupportRecordModeClient());

            AccountMapData accountMapData = RedisDBOperation.selectGuestAccount(request.getDeviceId(), request.getUserUid());

            if (accountMapData == null) {
                String url = cloudUrl + "/admin-downloadUserData";
                DeviceIdUser deviceIdUser = new DeviceIdUser("huF9NVzVKRQ^F&Nb8Cgmnhtl#Nvolu", request.getDeviceId());
                UserData userDataFromDevice = HttpUtil.getUserDataFromCloud(url, deviceIdUser);
                if (userDataFromDevice == null) {
                    throw new BusinessException("没有device id" + request.getDeviceId() + "绑定的游客账号");
                }
                accountMapData = new AccountMapData(request.getDeviceId(), request.getUserUid());
                RedisDBOperation.insertGuestAccount(request.getUserUid(), request.getDeviceId(), accountMapData);

                log.info("device id" + request.getDeviceId() + "找到已绑定的游客账号" + request.getUserUid());

                Map<String, Object> foundGuestAccountSummary = new HashMap<>();

                foundGuestAccountSummary.put("uid", request.getUserUid());
                foundGuestAccountSummary.put("playerName", userDataFromDevice.getName());
                foundGuestAccountSummary.put("trophy", userDataFromDevice.getTrophy());
                map.put("foundGuestAccountSummary", foundGuestAccountSummary);
                return map;
            }


            String linkedPlayerUid = accountMapData.getLinkedPlayerUid();
            if (linkedPlayerUid.equals(request.getUserUid())) {
                log.info("device id" + request.getDeviceId() + "要恢复的游客账号就是当前账号");
                map.put("isActiveGuestAccount", true);
                return map;
            }

            log.info("device id" + request.getDeviceId() + "找到已绑定的游客账号" + linkedPlayerUid);
            UserData existPlayerData = RedisDBOperation.selectUserData("User:" + linkedPlayerUid);

            Map<String, Object> foundGuestAccountSummary = new HashMap<>();

            foundGuestAccountSummary.put("uid", linkedPlayerUid);
            foundGuestAccountSummary.put("playerName", existPlayerData.getName());
            foundGuestAccountSummary.put("trophy", existPlayerData.getTrophy());
            map.put("foundGuestAccountSummary", foundGuestAccountSummary);

            return map;
        } catch (Exception e) {
            CommonUtils.responseException(request, e, request.getUserUid());
        } finally {
            ThreadLocalUtil.remove();
        }

        return null;
    }
}
