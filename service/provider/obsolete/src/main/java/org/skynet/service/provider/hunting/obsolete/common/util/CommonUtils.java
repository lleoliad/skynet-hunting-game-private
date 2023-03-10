package org.skynet.service.provider.hunting.obsolete.common.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.skynet.service.provider.hunting.obsolete.DBOperation.RedisDBOperation;
import org.skynet.service.provider.hunting.obsolete.common.exception.BusinessException;
import org.skynet.service.provider.hunting.obsolete.common.exception.ErrorDescription;
import org.skynet.service.provider.hunting.obsolete.common.result.ResponseEnum;
import org.skynet.service.provider.hunting.obsolete.config.SystemPropertiesConfig;
import org.skynet.service.provider.hunting.obsolete.enums.PlatformName;
import org.skynet.service.provider.hunting.obsolete.pojo.dto.BaseDTO;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.BulletReward;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.GunReward;
import org.skynet.components.hunting.user.dao.entity.UserData;
import org.skynet.service.provider.hunting.obsolete.pojo.environment.GameEnvironment;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;


/**
 * 统一处理request,response工具类
 */
@Slf4j
@Component
public class CommonUtils {

    @Data
    public class ProcessRequestParameter {
        private Boolean verifyToken;
        private Boolean verifyRequestTime;
        private Boolean needLockFunction;
    }


    public static void main(String[] args) {
        System.out.println(compareVersion("1.0.11"));
        System.out.println(compareVersion("1.0.01"));
    }

    public static boolean compareVersion(String version) {
        String ver = "1.0.11";
        String[] split = version.split("\\.");
        // return Integer.parseInt(split[2]) >= 11;
        return true;
    }

    private static final String requestObfuscateKey = "pe4WyFLi%M1vxXWICB@YaME1JqmDc1";
    public static String configPath = System.getProperty("user.dir") + File.separator + "src/main/resources/pay" + File.separator;

    public static void responseRemoveServer(Object obj) throws IllegalAccessException {
        Class<? extends Object> cls = obj.getClass();
        Field[] fields = cls.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Field f = fields[i];
            f.setAccessible(true);
            if (f.getName().contains("server")) {
                setFieldValueByFieldName(f.getName(), obj, null);
            }
        }
    }

    ;


    private static void setFieldValueByFieldName(String fieldName, Object object, String value) {
        try {
            // 获取obj类的字节文件对象
            Class c = object.getClass();
            // 获取该类的成员变量
            Field f = c.getDeclaredField(fieldName);
            // 取消语言访问检查
            f.setAccessible(true);
            // 给变量赋值
            f.set(object, value);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public static void requestProcess(BaseDTO request, Boolean verifyToken, Boolean IsSupportRecordModeClientServer) {

        if (verifyToken == null)
            verifyToken = true;

        // log.info("请求数据：" + JSON.toJSONString(request));

        if (verifyToken) {
            //验证token
            String backupToken = RedisDBOperation.selectUserToken(request.getUserUid());

            String userUid = JwtUtils.getUserUid(request.getUserToken());
            if (backupToken == null || !backupToken.equals(request.getUserToken()) || !userUid.equals(request.getUserUid())) {
                throw new BusinessException("玩家" + request.getUserUid() + "的token验证不通过");
            }

            UserData userData = RedisDBOperation.selectUserData("User:" + request.getUserUid());
            Long unixTimeNow = TimeUtils.getUnixTimeSecond();
            //查看是否被封禁
            if (userData.getServerOnly().getStartBlockTime() <= unixTimeNow && userData.getServerOnly().getEndBlockTime() >= unixTimeNow) {
                throw new BusinessException(request.getUserUid() + "该玩家正在被封禁");
            }

            // if (GameEnvironment.userDataMap.containsKey(request.getUserUid())) {
            //     // if (unixTimeNow - userData.getLastRequestTime() < 10) {
            //     //     log.info("玩家在10秒内有重复请求，抛出错误信息");
            //     //     throw new BusinessException("请求未完成，等待请求结束中");
            //     // }
            // }

            userData.setLastRequestTime(unixTimeNow);
        }

        if (request.getClientBuildInAppInfo().getRecordOnlyMode() == null) {
            request.getClientBuildInAppInfo().setRecordOnlyMode(false);
        }
        if (request.getClientBuildInAppInfo().getRecordOnlyMode() && !IsSupportRecordModeClientServer) {
            throw new BusinessException("当前服务器不支持录制模式");
        }

        if (request.getPlatform().equals(PlatformName.UnityEditor.getPlatform())) {

            if (!request.getAdminKey().equals("!2YySEF#0WrCZSOy*KF@#N0EOhkol2")) {

                throw new BusinessException("玩家上报来自editor,但是没有提供正确的admin key");
            }

        }
        //记录这次请求的客户端版本
        // log.info("登录的用户:" + request.getUserUid());
        // log.info("登录的版本:" + request.getGameVersion());
        // log.info("登录的平台:" + request.getPlatform());

    }

    public static void processRequest(BaseDTO request, Boolean verifyToken, ProcessRequestParameter parameters) {


    }

    /**
     * 生产环境必须验证用户
     *
     * @param adminKey
     */
    public static void processAdminRequest(String adminKey) {

        if (SystemPropertiesConfig.PRODUCTION) {

            if (!adminKey.equals("huF9NVzVKRQ^F&Nb8Cgmnhtl#Nvolu")) {

                throw new BusinessException("鉴权失败", -1);
            }
        }


    }

    /**
     * 正确返回时添加时间和正确返回码
     *
     * @param code
     * @return
     */
    public static Map<String, Object> responsePrepare(Integer code) {

        if (code == null) {
            code = ResponseEnum.SUCCESS.getCode();
        }
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("code", code);
        map.put("serverTime", TimeUtils.getUnixTimeSecond());
        // log.warn("获取到的服务器时间：{}", TimeUtils.getUnixTimeSecond());
        return map;
    }

    /**
     * 错误信息返回体
     *
     * @param requestData
     * @param errorMessage
     * @param userUid
     */
    public static void responseException(Object requestData, Exception errorMessage, String userUid) {
        log.warn("出现错误信息===================================================");
        log.error("", errorMessage);
        log.warn("===================================================错误信息结束");
        ErrorDescription error = new ErrorDescription(requestData, errorMessage.getMessage());
        log.error(error.toString());
        try {
            //因为userDataMap用的是concurrentHashMap，所以这里的userUid一旦是null，就会导致报错
            GameEnvironment.userDataMap.remove(userUid);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(error.toString(), ResponseEnum.DATA_NOT_EXIST.getCode());
        }
        throw new BusinessException(error.toString(), ResponseEnum.ERROR.getCode());
    }

    public static String decodeObfuscateRequest(String decode) {
        byte[] decodeBuffer = requestObfuscateKey.getBytes(StandardCharsets.UTF_8);
        byte[] obfuscatedRequestBuffer = decode.getBytes(StandardCharsets.UTF_8);

        for (int i = 0; i < obfuscatedRequestBuffer.length; i++) {
            int decodeBufferIndex = i % decodeBuffer.length;
            obfuscatedRequestBuffer[i] = (byte) (obfuscatedRequestBuffer[i] ^ decodeBuffer[decodeBufferIndex]);
        }
        return new String(obfuscatedRequestBuffer, StandardCharsets.UTF_8);
    }

    public static InputStream readP12File(String packageName) {
        InputStream input = null;
        try {
            String filepath = CommonUtils.configPath + "p12" + File.separator + packageName + ".p12";
            input = new FileInputStream(filepath);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return input;
    }

    public static String getStringExceptionStack(Throwable t) {
        StringWriter errorsWriter = new StringWriter();
        t.printStackTrace(new PrintWriter(errorsWriter));
        return errorsWriter.toString();
    }

    public static Properties readProperties(String fileName) {
        Properties properties = null;
        InputStream in = null;
        try {
            properties = new Properties();
            in = new BufferedInputStream(new FileInputStream(CommonUtils.configPath + fileName));
            properties.load(in);
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != in) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return properties;
    }

    public static boolean isEmpty(Map<?, ?> m) {
        return m == null || m.size() == 0;
    }

    public static JSONObject readMonitorConfig() {
        String fileName = "monitor.json";
        return readJsonFile(fileName);
    }

    private static JSONObject readJsonFile(String fileName) {
        // System.getProperty("user.dir")为获取根目录
        //File.separator为不同操作系统的分隔符，linux和win是不一样的
        //tempFilePath该字符串里面为我们配置文件的路径
        JSONObject jsonObject = null;
        InputStream input = null;

        String filepath = System.getProperty("user.dir") + File.separator + "config" + File.separator + fileName;
        log.info("load file --> " + filepath);
        try {
            input = new FileInputStream(filepath);
            StringBuilder sb = new StringBuilder();
            byte[] buffer = new byte[1024];
            int length = 0;
            length = input.read(buffer);

            while (length != -1) {
                sb.append(new String(buffer, 0, length));
                length = input.read(buffer);
            }
            String jsonString = sb.toString();

            //将字符串加载到jsonObject对象中
            jsonObject = JSON.parseObject(jsonString);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != input) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return jsonObject;
    }

    public static JSONObject readServerConfig() {
        String fileName = "server.json";
        return readJsonFile(fileName);
    }

    public static JSONObject readConfig() {
        String fileName = "config.json";
        return readJsonFile(fileName);
    }

    public static String readFile(String dir, String fileName) {
        File file;
        FileInputStream fis = null;
        DataInputStream dis = null;
        try {
            file = new File(dir + File.separator, fileName);
            if (null != file && file.isFile()) {
                fis = new FileInputStream(file);
                dis = new DataInputStream(fis);
                byte[] key = new byte[dis.available()];
                for (int i = 0; i < key.length; i++) {
                    key[i] = dis.readByte();
                }
                return new String(key, StandardCharsets.UTF_8).trim();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != dis) {
                try {
                    dis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (null != fis) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static File writeFile(String dir, String fileName, String streamData, boolean createNewFile, boolean append) {
        FileOutputStream fos = null;
        File newLogFile;
        try {
            if (null == fileName) {
                newLogFile = new File(dir);
                // 不存在则创建
                if (!newLogFile.exists()) {
                    newLogFile.mkdirs();
                }
            } else {
                newLogFile = new File(dir, fileName);
                if (createNewFile) {
                    if (newLogFile.exists()) {
                        newLogFile.delete();
                    }
                }
                // 不存在则创建
                if (!newLogFile.exists()) {
                    newLogFile.getParentFile().mkdirs();
                    newLogFile.createNewFile();
                }
            }
            if (null != streamData) {
                fos = new FileOutputStream(newLogFile, append);
                fos.write(streamData.getBytes(StandardCharsets.UTF_8));
                fos.flush();
            }
        } catch (Throwable t) {
            newLogFile = null;
            log.error(getStringExceptionStack(t));
        } finally {
            if (null != fos) {
                try {
                    fos.close();
                } catch (Throwable t) {
                    log.error(getStringExceptionStack(t));
                }
            }
        }

        return newLogFile;
    }

    public static String removeString(String sourceStr, String removeStr, String separation) {
        StringBuilder str = new StringBuilder();
        for (String packageName : sourceStr.split(separation)) {
            if (packageName.equals(removeStr)) {
                continue;
            }
            if (str.length() > 0) {
                str.append(",");
            }
            str.append(packageName);
        }
        return str.toString();
    }

    public static boolean isIpLegal(String str) {
        //检查ip是否为空
        if (str == null) {
            return false;
        }
        //检查ip长度，最短x.x.x.x（7位），最长为xxx.xxx.xxx.xxx（15位）
        if (str.length() < 7 || str.length() > 15) {
            return false;
        }
        //对输入字符串的首末字符判断，如果是“.”，则是非法ip
        if (str.charAt(0) == '.' || str.charAt(str.length() - 1) == '.') {
            return false;
        }
        //按"."分割字符串，并判断分割出来的个数，如果不是4个，则是非法ip
        String[] arr = str.split("\\.");
        if (arr.length != 4) {
            return false;
        }
        //对分割出来的字符串进行单独判断
        for (String s : arr) {
            //如果每个字符串不是一位字符，且以'0'开头，则是非法ip,如01.02.03.004
            if (s.length() > 1 && s.charAt(0) == '0') {
                return false;
            }
            //对每个字符串的每个字符进行逐一判断，如果不是数字0-9则是非法ip
            for (int j = 0; j < s.length(); j++) {
                if (s.charAt(j) < '0' || s.charAt(j) > '9') {
                    return false;
                }
            }
        }
        //对拆分的每一个字符串进行转换成数字，并判断是否在0-255
        for (int i = 0; i < arr.length; i++) {
            int temp = Integer.parseInt(arr[i]);
            if (i == 0) {
                if (temp < 1 || temp > 255) {
                    return false;
                }
            } else {
                if (temp < 0 || temp > 255) {
                    return false;
                }
            }
        }
        return true;
    }

    public static void writeProperties(Properties properties, String fileName) {
        FileWriter fw = null;
        try {
            fw = new FileWriter(CommonUtils.configPath + fileName);
            properties.store(fw, "key-value");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != fw) {
                    fw.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static Map<Integer, Integer> combineGunIdAndCountArrayToGunCountMap(List<Integer> gunIds, List<Integer> gunCounts) {
        if (gunIds.size() != gunCounts.size()) {
            throw new BusinessException("function combineGunIdAndCountArrayToGunCountMap gunIds.length != gunCounts.length");
        }
        Map<Integer, Integer> countMap = Maps.newHashMap();
        for (int i = 0; i < gunIds.size(); i++) {
            int gunId = gunIds.get(i);
            int gunCount = gunCounts.get(i);
            Integer countValue = countMap.getOrDefault(gunId, 0) + gunCount;
            countMap.put(gunId, countValue);
        }
        return countMap;
    }

    public static Map<Integer, Integer> convertBulletCountArrayToBulletCountMap(List<BulletReward> bulletCountArray) {
        Map<Integer, Integer> countMap = Maps.newHashMap();
        if (bulletCountArray != null) {
            for (int i = 0; i < bulletCountArray.size(); i++) {
                BulletReward bulletCountInfo = bulletCountArray.get(i);
                Integer bulletId = bulletCountInfo.getBulletId();
                Integer bulletCount = bulletCountInfo.getCount();
                Integer countValue = countMap.getOrDefault(bulletId, 0) + bulletCount;
                countMap.put(bulletId, countValue);
            }
        }
        return countMap;
    }

    public static List<BulletReward> convertBulletCountMapToBulletCountArray(Map<Integer, Integer> countMap) {
        List<BulletReward> result = Lists.newArrayList();
        for (Map.Entry<Integer, Integer> entry : countMap.entrySet()) {
            BulletReward bulletReward = new BulletReward(entry.getKey(), entry.getValue());
            result.add(bulletReward);
        }

        return result;
    }

    public static Map<Integer, Integer> convertGunCountArrayToGunCountMap(List<GunReward> gunCountArray) {
        Map<Integer, Integer> countMap = Maps.newHashMap();
        if (gunCountArray != null) {
            for (int i = 0; i < gunCountArray.size(); i++) {
                GunReward gunCountInfo = gunCountArray.get(i);
                Integer gunId = gunCountInfo.getGunId();
                Integer gunCount = gunCountInfo.getCount();
                Integer countValue = countMap.getOrDefault(gunId, 0) + gunCount;
                countMap.put(gunId, countValue);
            }
        }
        return countMap;
    }

    public static List<GunReward> convertGunCountMapToGunCountArray(Map<Integer, Integer> gunCountMap) {
        List<GunReward> result = Lists.newArrayList();
        for (Map.Entry<Integer, Integer> entry : gunCountMap.entrySet()) {
            GunReward gunReward = new GunReward(entry.getKey(), entry.getValue());
            result.add(gunReward);
        }

        return result;
    }

    public static Double randomFloat(Double min, Double max) {
        return Math.random() * (max - min) + min;
    }


}
