package org.skynet.service.provider.hunting.obsolete.controller.admin;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.services.androidpublisher.AndroidPublisherScopes;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.skynet.service.provider.hunting.obsolete.DBOperation.RedisDBOperation;
import org.skynet.service.provider.hunting.obsolete.common.Path;
import org.skynet.service.provider.hunting.obsolete.common.exception.BusinessException;
import org.skynet.service.provider.hunting.obsolete.common.util.CommonUtils;
import org.skynet.service.provider.hunting.obsolete.common.util.HttpUtil;
import org.skynet.service.provider.hunting.obsolete.pojo.dto.DownloadDataDTO;
import org.skynet.service.provider.hunting.obsolete.pojo.dto.DownloadWholeDataDTO;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.OpponentProfile;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.PlayerControlRecordDocData;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.PlayerUploadWholeMatchControlRecordData;
import org.skynet.service.provider.hunting.obsolete.service.PlayerControlRecordDataService;
import org.skynet.service.provider.hunting.obsolete.service.RecordIndexService;
import org.skynet.starter.codis.service.CodisService;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Api(tags = "玩家控制记录")
@RestController
@Slf4j
@RequestMapping("/huntingrival")
public class PlayerControlRecordController {

    @Resource
    private PlayerControlRecordDataService playerControlRecordDataService;

    // @Resource
    // private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private CodisService codisService;

//    @ApiOperation("创建已有所有录像的分布数据库")
//    @GetMapping("/createDatabase")
//    public Map<String, Object> createControlRecordDataDistributionDatabase(@RequestBody CreateDataBaseDTO createDataBaseDTO){
//
//
//
//    }

//    @GetMapping("pool/info")
//    @ApiOperation("获取匹配池的信息")
//    public Map<String,Object> getPoolInformation(@RequestBody CheckDBDTO dto){
//
//        RangeInt trophySegmentRange = new RangeInt(dto.getTrophySegmentRange()[0],dto.getTrophySegmentRange()[1]);
//        CommonUtils.processAdminRequest(dto.getAdminKey());
//        String key = Path.getMatchControlRecordsPool(
//                dto.getGameVersion(),
//                dto.getMatchId(),
//                trophySegmentRange
//        );
//        MatchControlRecordsPoolMetaData poolTrophySegmentMetaData = playerControlRecordDataService.getPoolTrophySegmentMetaData(key);
//
//        Map<String,Object> resultMap = new LinkedHashMap<>();
//
//        resultMap.put("poolInformation",poolTrophySegmentMetaData);
//        return resultMap;
//    }


    @PostMapping("/download/wholeData")
    @ApiOperation("下载整局匹配录像")
    public Map<String, Object> getWholePlayerData(@RequestBody DownloadWholeDataDTO request) throws IOException {

//        BufferedReader reader = null;
//        String srcFile =  request.getFilePath();
        try {
            DownloadDataDTO serverResponse = null;
//            reader = new BufferedReader(new FileReader(srcFile));
//            String temp;
            boolean flag = false;
            while (request.getCollectionPath() != null) {
//                if (!temp.equals("")){
//
//                }
//                log.warn(temp.trim());
//                request.setCollectionPath(temp.trim());
//                https://us-central1-wildhunthuntingclash.cloudfunctions.net/admin-batchDownloadDocuments
//                http://192.168.2.102:12001/xxhuntingxx-2876d/us-central1//admin-batchDownloadDocuments
//                http://47.242.75.245:8017/admin-batchDownloadDocuments  转发的正式服地址
                serverResponse = HttpUtil.serverResponse("https://us-central1-wildhunthuntingclash.cloudfunctions.net/admin-batchDownloadDocuments", request);
                do {
                    flag = false;
                    if (serverResponse.getDownloadData() != null && serverResponse.getDownloadData().size() != 0) {
                        for (int i = 0; i < serverResponse.getDownloadData().size(); i++) {
                            Map<String, Object> map = JSONObject.parseObject(serverResponse.getDownloadData().get(i).toString(), new TypeReference<Map<String, Object>>() {
                            });
                            PlayerUploadWholeMatchControlRecordData saveData = JSONObject.parseObject(map.get("docDataJson").toString(), PlayerUploadWholeMatchControlRecordData.class);
//                            log.warn("输出保存的savaData数据：{}",saveData);
                            playerControlRecordDataService.downloadPlayerWholeData(false, "1.0.11", saveData);
                        }
                    }
                    if (serverResponse.getCursorData() != null) {
                        flag = true;
                        request.setCursorDocId(serverResponse.getCursorData());
                        log.warn(JSONObject.toJSONString(request));
                        Thread.sleep(2000);
                        serverResponse = HttpUtil.serverResponse("https://us-central1-wildhunthuntingclash.cloudfunctions.net/admin-batchDownloadDocuments", request);
                    }
                } while (flag);

                request.setCursorDocId(null);

            }

            return CommonUtils.responsePrepare(null);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(e.getMessage());
        } finally {
//            reader.close();
        }

    }


    @PostMapping("/download/AiProfiles")
    @ApiOperation("下载头像")
    public Map<String, Object> getAiProfiles(@RequestBody DownloadWholeDataDTO request) throws IOException {


        try {
            DownloadDataDTO serverResponse = null;
            int aiProfileId = 1;
            serverResponse = HttpUtil.serverResponse("https://us-central1-wildhunthuntingclash.cloudfunctions.net/admin-batchDownloadDocuments", request);
            boolean flag = false;
            do {
                flag = false;
                if (serverResponse.getDownloadData() != null && serverResponse.getDownloadData().size() != 0) {
                    for (int i = 0; i < serverResponse.getDownloadData().size(); i++) {
                        Map<String, Object> map = JSONObject.parseObject(serverResponse.getDownloadData().get(i).toString(), new TypeReference<Map<String, Object>>() {
                        });
                        OpponentProfile saveData = JSONObject.parseObject(map.get("docDataJson").toString(), OpponentProfile.class);
                        String collectionPath = Path.getDefaultAiProfileCollectionPath();
                        String redisAiProfileId = collectionPath + ":" + aiProfileId;
                        log.info("redisAiProfileId=" + aiProfileId++);
                        RedisDBOperation.insertOpponentProfile(redisAiProfileId, saveData);
                    }
                }
                if (serverResponse.getCursorData() != null) {
                    flag = true;
                    request.setCursorDocId(serverResponse.getCursorData());
                    log.warn(JSONObject.toJSONString(request));
                    serverResponse = HttpUtil.serverResponse("https://us-central1-wildhunthuntingclash.cloudfunctions.net/admin-batchDownloadDocuments", request);
                }
            } while (flag);

            return CommonUtils.responsePrepare(null);
        } catch (Exception e) {

            throw new BusinessException(e.getMessage());
        }

    }

    public static int total;

    @Resource
    private RecordIndexService recordIndexService;

    private static HashMap<String, String> indexHashMap = new HashMap<>();


    private static List<RecordIndex> indexList = new ArrayList<>();


    @PostMapping("/download/singleData")
    public Map<String, Object> resolveWholeData(@RequestBody DownloadWholeDataDTO request) throws IOException {
        try {

            DownloadDataDTO serverResponse = null;

            boolean flag = false;
            serverResponse = HttpUtil.serverResponse("https://us-central1-wildhunthuntingclash.cloudfunctions.net/admin-batchDownloadDocuments", request);

            do {
                flag = false;
                if (serverResponse.getDownloadData() != null && serverResponse.getDownloadData().size() != 0) {
                    for (int i = 0; i < serverResponse.getDownloadData().size(); i++) {
                        Map<String, Object> map = JSONObject.parseObject(serverResponse.getDownloadData().get(i).toString(), new TypeReference<Map<String, Object>>() {
                        });
                        PlayerControlRecordDocData data = JSONObject.parseObject(map.get("docDataJson").toString(), PlayerControlRecordDocData.class);
                        String docId = map.get("docId").toString();
                        String key = Path.getMatchSingleRoundControlRecordsPoolCollectionPath(
                                "1.0.11",
                                data.getAnimalId(),
                                data.getAnimalRouteUid(),
                                data.getGunId(),
                                data.getGunLevel(),
                                data.getBulletId(),
                                data.getWindId(),
                                data.getAverageShowPrecision()
                        );
                        RecordIndex recordIndex = new RecordIndex(null,
                                docId,
                                key,
                                data.getPlayerUid(),
                                data.getGameVersion(),
                                data.getRecordVersion().longValue(),
                                data.getAnimalRouteUid().toString(),
                                data.getAnimalId().longValue(),
                                data.getBulletId().longValue(),
                                data.getGunId(),
                                data.getGunLevel(),
                                data.getWindId(),
                                data.getIsAnimalKilled(),
                                data.getFinalScore(),
                                data.getAverageShowPrecision(),
                                data.getSource().getType(),
                                data.getUploadTime()
                        );

                        if (!indexHashMap.containsKey(docId)) {
                            indexHashMap.put(docId, docId);
                            indexList.add(recordIndex);
                        }

                        playerControlRecordDataService.downloadSingleData(data);

                        //                        System.out.println("12313");
//                        recordIndexService.selectAndSave(indexList);

                        if (indexList.size() >= 10000) {
                            recordIndexService.saveBatch(indexList);
                            indexList.clear();
                        }

//                        try {
//                            PlayerControlRecordDocData data = JSONObject.parseObject(map.get("docDataJson").toString(), PlayerControlRecordDocData.class);
//                            playerControlRecordDataService.downloadSingleData(data);
//                        }catch (Exception e){
//                            e.printStackTrace();
//                            log.warn("Json转换失败" + map.get("docDataJson").toString());
//                        }
//                        docDataJson -> {"bulletId":2,"gunId":12,"isAnimalKilled":true,"animalId":106,"gameVersion":"1.0.10","uid":"---EMbA-f3icJ2m4hlJpel_wivwBbo","gunLevel":5,"finalScore":3233,"rawDataBase64":"3gAQqVBsYXllclVpZL53UE4tWndSQTA5RkFwcE1LUEdqZ1NHUnVGYmd0T2irR2FtZVZlcnNpb26mMS4wLjEwrVJlY29yZFZlcnNpb24DqVJlY29yZFVpZL4tLS1FTWJBLWYzaWNKMm00aGxKcGVsX3dpdndCYm+wUmVjb3JkRGF0YVNvdXJjZQG8QWxsQ29udHJvbFNlZ21lbnRSZWNvcmRzRGF0YZGFt1N0YXJ0QWltQXRvbUNvbnRyb2xEYXRhgqxTdGFydEFpbVRpbWXLP/oiQEAAAAC4QWltSW5BbmltYWxMb2NhbFBvc2l0aW9uk8u/+TPTgAAAAMvAIUX7gAAAAMu/yA/UgAAAALdEcmFnZ2luZ0NvbnRyb2xEYXRhTGlzdNwAJZLLP/oiQEAAAACTy7/5MkfgAAAAy8AhQVZgAAAAy7/IiQ+AAAAAkss//ETxAAAAAJPLv+Gg3yAAAADLwBVHWYAAAADLP7UXEYAAAACSyz/+IxtAAAAAk8s/pqbEQAAAAMu/4vXz4AAAAMs/s6tPwAAAAJLLP/+9AKAAAACTyz+4qS4gAAAAy7/Xih+gAAAAyz+8fB/AAAAAkss///zMAAAAAJPLP8RnrGAAAADLv8Nr3kAAAADLP8T8SKAAAACSy0AAHkugAAAAk8s/zgsj4AAAAMu/nRhkoAAAAMs/zcQO4AAAAJLLQAA+MuAAAACTyz/R79ggAAAAyz+OeO7AAAAAyz/RgP2AAAAAkstAAF4MoAAAAJPLP9P7hqAAAADLP6ngx0AAAADLP9M+bsAAAACSy0AAfeZgAAAAk8s/1F/jwAAAAMs/wmVq4
                    }
                }
                if (serverResponse.getCursorData() != null) {
                    flag = true;
                    request.setCursorDocId(serverResponse.getCursorData());
                    log.warn(JSONObject.toJSONString(request));
                    Thread.sleep(2000);
                    serverResponse = HttpUtil.serverResponse("https://us-central1-wildhunthuntingclash.cloudfunctions.net/admin-batchDownloadDocuments", request);
                }
            } while (flag);
            recordIndexService.saveBatch(indexList);
            indexList.clear();
            return CommonUtils.responsePrepare(null);
        } catch (Exception e) {
            log.warn("错误信息{}:", e.toString());
            e.printStackTrace();
            // RedisConnectionUtils.unbindConnection(Objects.requireNonNull(redisTemplate.getConnectionFactory()));
            log.warn("抛出异常后打印request:{}", JSONObject.toJSONString(request));
            resolveWholeData(request);
//            throw new BusinessException(e.toString());
        } finally {
            // RedisConnectionUtils.unbindConnection(Objects.requireNonNull(redisTemplate.getConnectionFactory()));
        }
        return CommonUtils.responsePrepare(null);
    }

    @PostMapping("/download/userData")
    public void downloadUserData() {

    }


    @PostMapping("/test/test")
    public void testClassloader() {
        try {

//            List<String> scopes = new ArrayList<String>();
//            scopes.add(AndroidPublisherScopes.ANDROIDPUBLISHER);
//            ClassLoader classLoader = IAPServiceImpl.class.getClassLoader();
//            GoogleCredential credential = GoogleCredential.fromStream(classLoader.getResourceAsStream("static/718144022281-ae30eafb2c5d.json"))
//                    .createScoped(scopes);


            List<String> scopes = new ArrayList<>();
            scopes.add(AndroidPublisherScopes.ANDROIDPUBLISHER);
            ResourceLoader resourceLoader = new DefaultResourceLoader();
            org.springframework.core.io.Resource resource = resourceLoader.getResource("classpath:static/718144022281-ae30eafb2c5d.json");
            File file = resource.getFile();
            BufferedInputStream inputStream = FileUtil.getInputStream(file);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String data = null;
            while ((data = bufferedReader.readLine()) != null) {
                System.out.println(data);
            }
//            GoogleCredential applicationDefault = GoogleCredential.getApplicationDefault();
//            log.warn("默认的credential数据：{}", JSONUtil.toJsonStr(applicationDefault));
            GoogleCredential credential = GoogleCredential.fromStream(resource.getInputStream()).createScoped(scopes);
            log.warn("生成的credential数据：{}", JSONUtil.toJsonStr(credential));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
