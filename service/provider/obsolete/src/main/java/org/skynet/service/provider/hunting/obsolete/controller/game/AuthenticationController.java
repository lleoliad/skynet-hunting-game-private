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

@Api(tags = "????????????")
@RestController
@RequestMapping("/huntingrival")
@Slf4j
public class AuthenticationController {

    @Resource
    private SystemPropertiesConfig systemPropertiesConfig;

    @Resource
    private ObsoleteUserDataService obsoleteUserDataService;


    @PostMapping("auth-getAuthenticationProviderBindGameAccount")
    @ApiOperation("????????????????????????")
    @RepeatSubmit(interval = 60000)
    public Map<String, Object> getAuthenticationProviderBindGameAccount(@RequestBody AuthenticationDTO request) {

        try {
            String cloudUrl = systemPropertiesConfig.getCloudUrl();
            UserData bindPlayerData;
            CommonUtils.requestProcess(request, null, systemPropertiesConfig.getSupportRecordModeClient());
            obsoleteUserDataService.ensureUserDataIdempotence(request.getUserUid(), request.getUserDataUpdateCount(), request.getGameVersion());
            Map<String, Object> map = CommonUtils.responsePrepare(null);
            AccountSummaryData accountSummaryData = new AccountSummaryData();


            //google????????????
            if (request.getAuthenticationProvider() == AuthenticationProvider.Google) {
                log.warn("??????????????????????????????");
                //????????????????????????
                obsoleteUserDataService.googleAuthenticationValidate(request.getProviderUserId(), request.getIdToken());
                AccountMapData googleAccountData = RedisDBOperation.selectGoogleAccountMapData(request.getProviderUserId());
                if (googleAccountData == null) {
                    log.warn("????????????????????????????????????????????????????????????");
                    //https://asia-south1-huntingmasterrecord.cloudfunctions.net/admin-downloadUserData  ?????????
                    //https://us-central1-wildhunthuntingclash.cloudfunctions.net/admin-downloadUserData  ?????????
                    String url = cloudUrl + "/admin-downloadUserData";
                    GoogleIdUser googleIdUser = new GoogleIdUser("huF9NVzVKRQ^F&Nb8Cgmnhtl#Nvolu", request.getProviderUserId());
                    bindPlayerData = HttpUtil.getUserDataFromCloud(url, googleIdUser);
                    if (bindPlayerData == null) {
                        log.info("?????????????????????{}??????????????????????????????", request.getProviderUserId());
                        map.put("bindPlayerData", null);
                        return map;
                    }
                    AccountMapData googleAccountMapData = new AccountMapData(request.getProviderUserId(), bindPlayerData.getUuid());
                    RedisDBOperation.insertGoogleAccountMapData(request.getProviderUserId(), googleAccountMapData);

                    if (RedisDBOperation.checkKeyExist("User:" + bindPlayerData.getUuid())) {
                        log.warn("??????????????????????????????????????????");
                        bindPlayerData = RedisDBOperation.selectUserData("User:" + bindPlayerData.getUuid());
                    } else {
                        log.warn("????????????????????????????????????????????????");
                        RedisDBOperation.insertUserData(bindPlayerData);
                    }

                    String linkedPlayerUid = bindPlayerData.getUuid();
                    log.info("??????google??????" + request.getProviderUserId() + "????????????????????????" + linkedPlayerUid);
                    log.warn("??????google????????????????????????????????????{}", JSONUtil.toJsonStr(bindPlayerData));

                    accountSummaryData.setUid(linkedPlayerUid);
                    accountSummaryData.setPlayerName(bindPlayerData.getName());
                    accountSummaryData.setTrophy(bindPlayerData.getTrophy());
                    map.put("bindPlayerData", accountSummaryData);
                    return map;
                }

                log.warn("?????????????????????????????????,???????????????");
                String linkedPlayerUid = googleAccountData.getLinkedPlayerUid();
                bindPlayerData = RedisDBOperation.selectUserData("User:" + linkedPlayerUid);
                log.info("???????????????google?????????" + request.getProviderUserId() + "????????????????????????" + linkedPlayerUid);
                log.warn("??????google????????????????????????????????????{}", JSONUtil.toJsonStr(bindPlayerData));

                accountSummaryData.setUid(linkedPlayerUid);
                accountSummaryData.setPlayerName(bindPlayerData.getName());
                accountSummaryData.setTrophy(bindPlayerData.getTrophy());
                map.put("bindPlayerData", accountSummaryData);
                return map;

            } else if (request.getAuthenticationProvider() == AuthenticationProvider.Facebook) {
                log.warn("??????????????????Facebook??????");

                obsoleteUserDataService.facebookAuthenticationValidate(request.getProviderUserId(), request.getIdToken());

                AccountMapData facebookAccountData = RedisDBOperation.selectFacebookAccountMapData(request.getProviderUserId());

                if (facebookAccountData == null) {
                    log.warn("????????????????????????????????????????????????????????????");
                    String url = cloudUrl + "/admin-downloadUserData";
                    FacebookIdUser facebookIdUser = new FacebookIdUser("huF9NVzVKRQ^F&Nb8Cgmnhtl#Nvolu", request.getProviderUserId());
                    bindPlayerData = HttpUtil.getUserDataFromCloud(url, facebookIdUser);
                    if (bindPlayerData == null) {
                        log.info("??????facebook?????????{}??????????????????????????????", request.getProviderUserId());
                        map.put("bindPlayerData", null);
                        return map;
                    }
                    //???facebookAccountMapData,??????map
                    AccountMapData facebookAccountMapData = new AccountMapData(request.getProviderUserId(), bindPlayerData.getUuid());
                    RedisDBOperation.insertFacebookAccountMapData(request.getProviderUserId(), facebookAccountMapData);

                    if (RedisDBOperation.checkKeyExist("User:" + bindPlayerData.getUuid())) {
                        log.warn("??????????????????????????????????????????");
                        bindPlayerData = RedisDBOperation.selectUserData("User:" + bindPlayerData.getUuid());
                    } else {
                        log.warn("????????????????????????????????????????????????");
                        RedisDBOperation.insertUserData(bindPlayerData);
                    }

                    String linkedPlayerUid = bindPlayerData.getUuid();
                    log.info("???????????????facebook?????????" + request.getProviderUserId() + "????????????????????????" + linkedPlayerUid);
                    log.warn("??????facebook????????????????????????????????????{}", JSONUtil.toJsonStr(bindPlayerData));

                    accountSummaryData.setUid(linkedPlayerUid);
                    accountSummaryData.setPlayerName(bindPlayerData.getName());
                    accountSummaryData.setTrophy(bindPlayerData.getTrophy());
                    map.put("bindPlayerData", accountSummaryData);
                    return map;
                }

                log.warn("??????????????????????????????????????????????????????");
                String linkedPlayerUid = facebookAccountData.getLinkedPlayerUid();
                bindPlayerData = RedisDBOperation.selectUserData("User:" + linkedPlayerUid);
                log.info("???????????????facebook?????????" + request.getProviderUserId() + "?????????????????????" + linkedPlayerUid);
                log.warn("??????facebook????????????????????????????????????{}", JSONUtil.toJsonStr(bindPlayerData));

                accountSummaryData.setUid(linkedPlayerUid);
                accountSummaryData.setPlayerName(bindPlayerData.getName());
                accountSummaryData.setTrophy(bindPlayerData.getTrophy());
                map.put("bindPlayerData", accountSummaryData);
                return map;
            }

            log.info("??????????????????google??????facebook");
            return map;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }


    @PostMapping("auth-recoverAuthenticationProviderGameAccount")
    @ApiOperation("?????????????????????")
    @RepeatSubmit(interval = 60000)
    public Map<String, Object> recoverAuthenticationProviderGameAccount(@RequestBody AuthenticationDTO request) {
        log.warn("???????????????auth-recoverAuthenticationProviderGameAccount");
        try {
            UserData existPlayerData;
            ThreadLocalUtil.set(request.getServerTimeOffset());
            String cloudUrl = systemPropertiesConfig.getCloudUrl();
            CommonUtils.requestProcess(request, null, systemPropertiesConfig.getSupportRecordModeClient());
            obsoleteUserDataService.ensureUserDataIdempotence(request.getUserUid(), request.getUserDataUpdateCount(), request.getGameVersion());
            UserDataSendToClient userDataSendToClient = GameEnvironment.prepareSendToClientUserData();
            if (request.getAuthenticationProvider() == AuthenticationProvider.Google) {

                log.warn("??????????????????????????????");
                //????????????????????????
                obsoleteUserDataService.googleAuthenticationValidate(request.getProviderUserId(), request.getIdToken());

                AccountMapData googleAccountData = RedisDBOperation.selectGoogleAccountMapData(request.getProviderUserId());

                if (googleAccountData == null) {
                    log.warn("????????????????????????????????????????????????????????????");
                    String url = cloudUrl + "/admin-downloadUserData";
                    log.info("??????????????????" + url);
                    GoogleIdUser googleIdUser = new GoogleIdUser("huF9NVzVKRQ^F&Nb8Cgmnhtl#Nvolu", request.getProviderUserId());
                    existPlayerData = HttpUtil.getUserDataFromCloud(url, googleIdUser);
                    if (existPlayerData == null) {
                        throw new BusinessException("?????????google??????????????????" + request.getProviderUserId());
                    }
                    AccountMapData googleAccountMapData = new AccountMapData(request.getProviderUserId(), existPlayerData.getUuid());
                    RedisDBOperation.insertGoogleAccountMapData(request.getProviderUserId(), googleAccountMapData);

                    if (RedisDBOperation.checkKeyExist("User:" + existPlayerData.getUuid())) {
                        log.warn("??????????????????????????????????????????");
                        existPlayerData = RedisDBOperation.selectUserData("User:" + existPlayerData.getUuid());
                    } else {
                        log.warn("????????????????????????????????????????????????");
                        RedisDBOperation.insertUserData(existPlayerData);
                    }

                    String linkedPlayerUid = existPlayerData.getUuid();
                    log.info("??????google??????" + request.getProviderUserId() + "?????????????????????" + linkedPlayerUid);
                    log.warn("?????????????????????????????????{}", JSONUtil.toJsonStr(existPlayerData));
                    Map<String, Object> map = CommonUtils.responsePrepare(null);
                    BeanUtils.copyProperties(existPlayerData, userDataSendToClient);
                    obsoleteUserDataService.userDataSettlement(existPlayerData, userDataSendToClient, false, request.getGameVersion());
                    map.put("userData", userDataSendToClient);
                    map.put("privateKey", existPlayerData.getServerOnly().getPrivateKey());

                    // TODO ????????????????????????
                    String sv = JSON.toJSONString(map, SerializerFeature.WriteMapNullValue);
                    String siv = JSON.toJSONString(map);
                    if (sv.length() != siv.length()) {
                        map = JSON.parseObject(siv);
                        log.warn("???????????????:{}", siv);
                    }
                    return map;
                }
                log.warn("??????????????????????????????????????????????????????");
                String linkedPlayerUid = googleAccountData.getLinkedPlayerUid();
                existPlayerData = RedisDBOperation.selectUserData("User:" + linkedPlayerUid);
                log.info("??????google??????" + request.getProviderUserId() + "?????????????????????" + linkedPlayerUid);
                log.warn("?????????????????????????????????{}", JSONUtil.toJsonStr(existPlayerData));
                Map<String, Object> map = CommonUtils.responsePrepare(null);
                BeanUtils.copyProperties(existPlayerData, userDataSendToClient);
                obsoleteUserDataService.userDataSettlement(existPlayerData, userDataSendToClient, false, request.getGameVersion());
                map.put("userData", userDataSendToClient);
                map.put("privateKey", existPlayerData.getServerOnly().getPrivateKey());

                // TODO ????????????????????????
                String sv = JSON.toJSONString(map, SerializerFeature.WriteMapNullValue);
                String siv = JSON.toJSONString(map);
                if (sv.length() != siv.length()) {
                    map = JSON.parseObject(siv);
                    log.warn("???????????????:{}", siv);
                }
                return map;
            } else if (request.getAuthenticationProvider() == AuthenticationProvider.Facebook) {
                log.warn("??????????????????Facebook??????");

                obsoleteUserDataService.facebookAuthenticationValidate(request.getProviderUserId(), request.getIdToken());

                AccountMapData facebookAccountData = RedisDBOperation.selectFacebookAccountMapData(request.getProviderUserId());


                if (facebookAccountData == null) {
                    log.warn("????????????????????????????????????????????????????????????");
                    String url = cloudUrl + "/admin-downloadUserData";
                    log.info("??????????????????" + url);
                    FacebookIdUser facebookIdUser = new FacebookIdUser("huF9NVzVKRQ^F&Nb8Cgmnhtl#Nvolu", request.getProviderUserId());
                    existPlayerData = HttpUtil.getUserDataFromCloud(url, facebookIdUser);
                    if (existPlayerData == null) {
                        throw new BusinessException("?????????FaceBook??????????????????" + request.getProviderUserId());
                    }
                    //???facebookAccountMapData,??????map
                    AccountMapData facebookAccountMapData = new AccountMapData(request.getProviderUserId(), existPlayerData.getUuid());
                    RedisDBOperation.insertFacebookAccountMapData(request.getProviderUserId(), facebookAccountMapData);

                    if (RedisDBOperation.checkKeyExist("User:" + existPlayerData.getUuid())) {
                        log.warn("??????????????????????????????????????????");
                        existPlayerData = RedisDBOperation.selectUserData("User:" + existPlayerData.getUuid());
                    } else {
                        log.warn("????????????????????????????????????????????????");
                        RedisDBOperation.insertUserData(existPlayerData);
                    }

                    String linkedPlayerUid = existPlayerData.getUuid();
                    log.info("??????facebook??????" + request.getProviderUserId() + "?????????????????????" + linkedPlayerUid);
                    log.warn("?????????????????????????????????{}", JSONUtil.toJsonStr(existPlayerData));
                    Map<String, Object> map = CommonUtils.responsePrepare(null);
                    BeanUtils.copyProperties(existPlayerData, userDataSendToClient);
                    obsoleteUserDataService.userDataSettlement(existPlayerData, userDataSendToClient, false, request.getGameVersion());
                    map.put("userData", userDataSendToClient);
                    map.put("privateKey", existPlayerData.getServerOnly().getPrivateKey());

                    // TODO ????????????????????????
                    String sv = JSON.toJSONString(map, SerializerFeature.WriteMapNullValue);
                    String siv = JSON.toJSONString(map);
                    if (sv.length() != siv.length()) {
                        map = JSON.parseObject(siv);
                        log.warn("???????????????:{}", siv);
                    }
                    return map;
                }

                log.warn("??????????????????????????????????????????????????????");
                String linkedPlayerUid = facebookAccountData.getLinkedPlayerUid();
                existPlayerData = RedisDBOperation.selectUserData("User:" + linkedPlayerUid);
                log.info("??????facebook??????" + request.getProviderUserId() + "?????????????????????" + linkedPlayerUid);
                log.warn("?????????????????????????????????{}", JSONUtil.toJsonStr(existPlayerData));
                Map<String, Object> map = CommonUtils.responsePrepare(null);
                BeanUtils.copyProperties(existPlayerData, userDataSendToClient);
                obsoleteUserDataService.userDataSettlement(existPlayerData, userDataSendToClient, false, request.getGameVersion());
                map.put("userData", userDataSendToClient);
                map.put("privateKey", existPlayerData.getServerOnly().getPrivateKey());

                // TODO ????????????????????????
                String sv = JSON.toJSONString(map, SerializerFeature.WriteMapNullValue);
                String siv = JSON.toJSONString(map);
                if (sv.length() != siv.length()) {
                    map = JSON.parseObject(siv);
                    log.warn("???????????????:{}", siv);
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
    @ApiOperation("?????????????????????????????????")
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
                throw new BusinessException("??????????????????");
            }

            if (request.getAuthenticationProvider().equals(AuthenticationProvider.Google)) {
                log.info("google????????????");

                GoogleIdToken.Payload payload = obsoleteUserDataService.googleAuthenticationValidate(request.getProviderUserId(), request.getIdToken());

                //????????????????????????????????????
                AccountMapData googleAccountMapData = RedisDBOperation.selectGoogleAccountMapData(request.getProviderUserId());

                if (googleAccountMapData == null) {
                    log.warn("????????????????????????????????????????????????????????????");
                    String url = cloudUrl + "/admin-downloadUserData";
                    log.info("??????????????????" + url);
                    GoogleIdUser googleIdUser = new GoogleIdUser("huF9NVzVKRQ^F&Nb8Cgmnhtl#Nvolu", request.getProviderUserId());
                    UserData userDataFromCloud = HttpUtil.getUserDataFromCloud(url, googleIdUser);
                    if (userDataFromCloud != null) {
                        existPlayerData = userDataFromCloud;
                        log.info("google????????????,??????" + request.getProviderUserId() + "???????????????????????????.");
                        googleAccountMapData = new AccountMapData(request.getProviderUserId(), userDataFromCloud.getUuid());
                        RedisDBOperation.insertGoogleAccountMapData(request.getProviderUserId(), googleAccountMapData);

                        if (RedisDBOperation.checkKeyExist("User:" + existPlayerData.getUuid())) {
                            log.warn("??????????????????????????????????????????");
                            existPlayerData = RedisDBOperation.selectUserData("User:" + existPlayerData.getUuid());
                        } else {
                            log.warn("????????????????????????????????????????????????");
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
                    log.warn("?????????????????????????????????????????????");
                }


                if (googleAccountMapData != null) {
                    log.warn("?????????????????????????????????,{}", JSONUtil.toJsonStr(googleAccountMapData));
                    if (!googleAccountMapData.getLinkedPlayerUid().equals(request.getUserUid())) {
                        existPlayerData = RedisDBOperation.selectUserData("User:" + googleAccountMapData.getLinkedPlayerUid());
                        log.info("google????????????,??????" + request.getProviderUserId() + "???????????????????????????.");

                        Map<String, Object> map = CommonUtils.responsePrepare(null);
                        Map<String, Object> alreadyExistPlayerData = new HashMap<>();
                        alreadyExistPlayerData.put("uid", googleAccountMapData.getLinkedPlayerUid());
                        alreadyExistPlayerData.put("playerName", existPlayerData.getName());
                        alreadyExistPlayerData.put("trophy", existPlayerData.getTrophy());

                        map.put("alreadyExistPlayerData", alreadyExistPlayerData);
                        return map;
                    }
                }

                //??????map
                googleAccountMapData = new AccountMapData(request.getProviderUserId(), request.getUserUid());
                RedisDBOperation.insertGoogleAccountMapData(request.getProviderUserId(), googleAccountMapData);

                String pictureUrl = (String) payload.get("picture");
                //????????????
                obsoleteUserDataService.saveUserProfileImage(pictureUrl, request.getUserUid());

                //??????????????????????????????
                if (StringUtils.isEmpty(existPlayerData.getLinkedAuthProviderData().getGooglePlayUserId())) {

                    existPlayerData.getLinkedAuthProviderData().setGooglePlayUserId(request.getProviderUserId());
                    userDataSendToClient.setLinkedAuthProviderData(existPlayerData.getLinkedAuthProviderData());
                }

                //??????????????????deviceId
                if (request.getDeviceId() != null) {
                    obsoleteUserDataService.deleteGuestAccountData(existPlayerData, request.getDeviceId());
                }
                obsoleteUserDataService.userDataSettlement(existPlayerData, userDataSendToClient, true, request.getGameVersion());

                Map<String, Object> map = CommonUtils.responsePrepare(null);
                map.put("userData", userDataSendToClient);
                return map;
            } else if (request.getAuthenticationProvider() == AuthenticationProvider.Facebook) {

                log.info("facebook ????????????");

                String serverToken = obsoleteUserDataService.facebookAuthenticationValidate(request.getProviderUserId(), request.getIdToken());
                log.info("facebook ????????????????????????");

                //????????????????????????????????????
                AccountMapData facebookAccountMapData = RedisDBOperation.selectFacebookAccountMapData(request.getProviderUserId());

                if (facebookAccountMapData == null) {
                    log.warn("????????????????????????????????????????????????????????????");
                    String url = cloudUrl + "/admin-downloadUserData";
                    FacebookIdUser facebookIdUser = new FacebookIdUser("huF9NVzVKRQ^F&Nb8Cgmnhtl#Nvolu", request.getProviderUserId());
                    UserData userDataFromCloud = HttpUtil.getUserDataFromCloud(url, facebookIdUser);
                    if (userDataFromCloud != null) {
                        existPlayerData = userDataFromCloud;
                        log.info("facebook ????????????,??????" + request.getProviderUserId() + "???????????????????????????.");
                        //???facebookAccountMapData,??????map
                        facebookAccountMapData = new AccountMapData(request.getProviderUserId(), userDataFromCloud.getUuid());
                        RedisDBOperation.insertFacebookAccountMapData(request.getProviderUserId(), facebookAccountMapData);

                        if (RedisDBOperation.checkKeyExist("User:" + userDataFromCloud.getUuid())) {
                            log.warn("??????????????????????????????????????????");
                            existPlayerData = RedisDBOperation.selectUserData("User:" + existPlayerData.getUuid());
                        } else {
                            log.warn("????????????????????????????????????????????????");
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
                    log.warn("?????????????????????????????????????????????");
                }

                if (facebookAccountMapData != null) {

                    if (!facebookAccountMapData.getLinkedPlayerUid().equals(request.getUserUid())) {
                        existPlayerData = RedisDBOperation.selectUserData("User:" + facebookAccountMapData.getLinkedPlayerUid());
                        log.info("facebook ????????????,??????" + request.getProviderUserId() + "???????????????????????????.");
                        Map<String, Object> map = CommonUtils.responsePrepare(null);
                        Map<String, Object> alreadyExistPlayerData = new HashMap<>();
                        alreadyExistPlayerData.put("uid", facebookAccountMapData.getLinkedPlayerUid());
                        alreadyExistPlayerData.put("playerName", existPlayerData.getName());
                        alreadyExistPlayerData.put("trophy", existPlayerData.getTrophy());

                        map.put("alreadyExistPlayerData", alreadyExistPlayerData);
                        return map;
                    }
                }

                //?????????????????????,??????map
                facebookAccountMapData = new AccountMapData(request.getProviderUserId(), request.getUserUid());
                RedisDBOperation.insertFacebookAccountMapData(request.getProviderUserId(), facebookAccountMapData);

                //????????????
                String profileImageUrl = "https://graph.facebook.com/v14.0/" + request.getProviderUserId() + "/picture?type=square&access_token=" + serverToken;
                obsoleteUserDataService.saveUserProfileImage(profileImageUrl, request.getUserUid());

                //??????????????????????????????
                if (StringUtils.isEmpty(existPlayerData.getLinkedAuthProviderData().getFacebookUserId())) {

                    existPlayerData.getLinkedAuthProviderData().setFacebookUserId(request.getProviderUserId());
                    userDataSendToClient.setLinkedAuthProviderData(existPlayerData.getLinkedAuthProviderData());
                }

                //??????????????????deviceId
                if (request.getDeviceId() != null) {
                    obsoleteUserDataService.deleteGuestAccountData(existPlayerData, request.getDeviceId());
                }
                obsoleteUserDataService.userDataSettlement(existPlayerData, userDataSendToClient, true, request.getGameVersion());

                Map<String, Object> map = CommonUtils.responsePrepare(null);
                map.put("userData", userDataSendToClient);
                return map;
            }

            throw new BusinessException("????????????????????????:" + request.getAuthenticationProvider());

        } catch (Exception e) {
            CommonUtils.responseException(request, e, request.getUserUid());
        } finally {
            ThreadLocalUtil.remove();
        }
        return null;
    }

    @PostMapping("auth-recoverGuestGameAccount")
    @ApiOperation("??????????????????")
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
                    throw new BusinessException("??????device id" + request.getDeviceId() + "?????????????????????");
                }
                Map<String, Object> map = CommonUtils.responsePrepare(null);
                map.put("userData", userDataFromDevice);
                map.put("privateKey", userDataFromDevice.getServerOnly().getPrivateKey());
                return map;
            }

            String linkedPlayerUid = accountMapData.getLinkedPlayerUid();
            UserData existPlayerData = RedisDBOperation.selectUserData("User:" + linkedPlayerUid);
            log.info("??????device id" + request.getDeviceId() + "?????????????????????" + JSONObject.toJSONString(existPlayerData));

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
    @ApiOperation("????????????device id???????????????????????????")
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
                    throw new BusinessException("??????device id" + request.getDeviceId() + "?????????????????????");
                }
                accountMapData = new AccountMapData(request.getDeviceId(), request.getUserUid());
                RedisDBOperation.insertGuestAccount(request.getUserUid(), request.getDeviceId(), accountMapData);

                log.info("device id" + request.getDeviceId() + "??????????????????????????????" + request.getUserUid());

                Map<String, Object> foundGuestAccountSummary = new HashMap<>();

                foundGuestAccountSummary.put("uid", request.getUserUid());
                foundGuestAccountSummary.put("playerName", userDataFromDevice.getName());
                foundGuestAccountSummary.put("trophy", userDataFromDevice.getTrophy());
                map.put("foundGuestAccountSummary", foundGuestAccountSummary);
                return map;
            }


            String linkedPlayerUid = accountMapData.getLinkedPlayerUid();
            if (linkedPlayerUid.equals(request.getUserUid())) {
                log.info("device id" + request.getDeviceId() + "??????????????????????????????????????????");
                map.put("isActiveGuestAccount", true);
                return map;
            }

            log.info("device id" + request.getDeviceId() + "??????????????????????????????" + linkedPlayerUid);
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
