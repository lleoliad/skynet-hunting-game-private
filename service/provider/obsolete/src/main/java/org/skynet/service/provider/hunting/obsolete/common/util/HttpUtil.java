package org.skynet.service.provider.hunting.obsolete.common.util;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.skynet.service.provider.hunting.obsolete.common.exception.BusinessException;
import org.skynet.service.provider.hunting.obsolete.pojo.dto.DownloadDataDTO;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.UserData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
public class HttpUtil {

    /**
     * 创建HttpURLConnection
     *
     * @param url
     * @return
     */
    public static HttpURLConnection getHttpConnection(URL url) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(true);
            connection.setConnectTimeout(60000);
            connection.setReadTimeout(60000);
            connection.setRequestProperty("Content-Type", "application/json");

        } catch (IOException e) {
            System.err.println("连接错误");
        } finally {
            if (null != connection) {
                connection.disconnect();
            }
        }
        return connection;
    }

    public static HttpURLConnection HttpConnectionByGet(URL url) {

        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

        } catch (IOException e) {
            System.err.println("连接错误");
        } finally {
            if (null != connection) {
                connection.disconnect();
            }
        }
        return connection;
    }

    /**
     * 谷歌云函数请求
     * 发送请求并接收返回结果
     *
     * @param postDTO
     * @return
     */
    public static DownloadDataDTO serverResponse(String url, Object postDTO) {
        HttpURLConnection httpURLConnection = null;

        StringBuilder lineBuffer = new StringBuilder();
        try {

            URL _url = new URL(url);
            httpURLConnection = HttpUtil.getHttpConnection(_url);
            httpURLConnection.setConnectTimeout(60000);
            httpURLConnection.setReadTimeout(60000);

            httpURLConnection.connect();
            //POST请求
            DataOutputStream out = new DataOutputStream(httpURLConnection.getOutputStream());

            String jsonString = JSONObject.toJSONString(postDTO);
            out.writeBytes(new String(jsonString.getBytes(), "utf-8"));
            out.flush();
            out.close();
            BufferedReader reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), "utf-8"));
            String _line;
            while ((_line = reader.readLine()) != null) {
                lineBuffer.append(_line);
            }

            return JSONObject.parseObject(lineBuffer.toString(), DownloadDataDTO.class);
        } catch (IOException e) {
            e.printStackTrace();
            throw new BusinessException("传输错误", -1);

        }

    }


    public static UserData getUserDataFromCloud(String url, Object postDTO) {
        HttpURLConnection httpURLConnection = null;

        StringBuilder lineBuffer = new StringBuilder();
        try {

            URL _url = new URL(url);
            httpURLConnection = HttpUtil.getHttpConnection(_url);
            httpURLConnection.setConnectTimeout(60000);
            httpURLConnection.setReadTimeout(60000);

            httpURLConnection.connect();
            //POST请求
            DataOutputStream out = new DataOutputStream(httpURLConnection.getOutputStream());

            String jsonString = JSONObject.toJSONString(postDTO);
            out.writeBytes(new String(jsonString.getBytes(), "utf-8"));
            out.flush();
            out.close();
            BufferedReader reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), "utf-8"));
            String _line;
            while ((_line = reader.readLine()) != null) {
                lineBuffer.append(_line);
            }

            Map<String, Object> map = JSONObject.parseObject(lineBuffer.toString(), new TypeReference<Map<String, Object>>() {
            });
            log.warn("接收到的原始JSON数据：{}", lineBuffer);
            Object userDataObject = map.get("userData");

            if (userDataObject == null) {
                return null;
            }

            return JSONUtil.toBean(userDataObject.toString(), UserData.class);
        } catch (IOException e) {
            e.printStackTrace();
            throw new BusinessException("传输错误", -1);

        }

    }


    public static Map<String, Object> getRankInfo(String url, Object postDTO) {
        HttpURLConnection httpURLConnection = null;

        StringBuilder lineBuffer = new StringBuilder();
        try {

            URL _url = new URL(url);
            httpURLConnection = HttpUtil.getHttpConnection(_url);
            httpURLConnection.setConnectTimeout(60000);
            httpURLConnection.setReadTimeout(60000);

            httpURLConnection.connect();
            //POST请求
            DataOutputStream out = new DataOutputStream(httpURLConnection.getOutputStream());

            String jsonString = JSONObject.toJSONString(postDTO);
            log.info("getRankInfo()方法发送的json数据为：{}", jsonString);
            out.writeBytes(new String(jsonString.getBytes(), StandardCharsets.UTF_8));
            out.flush();
            out.close();
            BufferedReader reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), StandardCharsets.UTF_8));
            String _line;
            while ((_line = reader.readLine()) != null) {
                lineBuffer.append(_line);
            }

            log.info("接收到的原始JSON数据：{}", lineBuffer);
            if (StringUtils.isEmpty(lineBuffer.toString())) {
                log.error("返回数据为空，直接返回null");
                return null;
            }
            Map<String, Object> map = JSONObject.parseObject(lineBuffer.toString(), new TypeReference<Map<String, Object>>() {
            });

            if (map.get("code") != null && (int) map.get("code") != 0) {
                log.error("战斗服务器返回数据失败");
                return null;
            }

            return map;

        } catch (IOException e) {
            e.printStackTrace();
            log.error("具体的错误信息：", e);
            throw new BusinessException("传输错误", -1);
        }

    }

    public static Map<String, Object> rankAddCoin(String url, Object postDTO) {
        HttpURLConnection httpURLConnection = null;

        StringBuilder lineBuffer = new StringBuilder();
        try {

            URL _url = new URL(url);
            httpURLConnection = HttpUtil.getHttpConnection(_url);
            httpURLConnection.setConnectTimeout(60000);
            httpURLConnection.setReadTimeout(60000);

            httpURLConnection.connect();
            //POST请求
            DataOutputStream out = new DataOutputStream(httpURLConnection.getOutputStream());

            String jsonString = JSONObject.toJSONString(postDTO);
            log.info("getRankInfo()方法发送的json数据为：{}", jsonString);
            out.writeBytes(new String(jsonString.getBytes(), StandardCharsets.UTF_8));
            out.flush();
            out.close();
            BufferedReader reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), StandardCharsets.UTF_8));
            String _line;
            while ((_line = reader.readLine()) != null) {
                lineBuffer.append(_line);
            }

            log.info("接收到的原始JSON数据：{}", lineBuffer);
            if (StringUtils.isEmpty(lineBuffer.toString())) {
                log.error("返回数据为空，直接返回null");
                return null;
            }
            Map<String, Object> map = JSONObject.parseObject(lineBuffer.toString(), new TypeReference<Map<String, Object>>() {
            });

            if (map.get("code") != null && (int) map.get("code") != 0) {
                log.error("战斗服务器返回数据失败");
                return null;
            }

            return map;

        } catch (IOException e) {
            e.printStackTrace();
            log.error("具体的错误信息：", e);
            throw new BusinessException("传输错误", -1);
        }

    }


    public static Map<String, Object> getFightInfo(String url, Object postDTO) {
        HttpURLConnection httpURLConnection = null;

        StringBuilder lineBuffer = new StringBuilder();
        try {

            URL _url = new URL(url);
            httpURLConnection = HttpUtil.getHttpConnection(_url);
            httpURLConnection.setConnectTimeout(60000);
            httpURLConnection.setReadTimeout(60000);

            httpURLConnection.connect();
            //POST请求
            DataOutputStream out = new DataOutputStream(httpURLConnection.getOutputStream());

            String jsonString = JSONObject.toJSONString(postDTO);
            log.info("getFightInfo()方法发送的json数据为：{}", jsonString);
            out.writeBytes(new String(jsonString.getBytes(), "utf-8"));
            out.flush();
            out.close();
            BufferedReader reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), "utf-8"));
            String _line;
            while ((_line = reader.readLine()) != null) {
                lineBuffer.append(_line);
            }

            log.info("接收到的原始JSON数据：{}", lineBuffer);
            if (StringUtils.isEmpty(lineBuffer.toString())) {
                log.error("返回数据为空，直接返回null");
                return null;
            }
            Map<String, Object> map = JSONObject.parseObject(lineBuffer.toString(), new TypeReference<Map<String, Object>>() {
            });

            if (map.get("code") != null && (int) map.get("code") != 0) {
                log.error("战斗服务器返回数据失败");
                return null;
            }

            return map;

        } catch (IOException e) {
            e.printStackTrace();
            log.error("具体的错误信息：", e);
            throw new BusinessException("传输错误", -1);
        }

    }


    public static Map<String, Object> getAIInfo(String url, Object postDTO) {
        HttpURLConnection httpURLConnection = null;

        StringBuilder lineBuffer = new StringBuilder();
        try {

            URL _url = new URL(url);
            httpURLConnection = HttpUtil.getHttpConnection(_url);
            httpURLConnection.setConnectTimeout(60000);
            httpURLConnection.setReadTimeout(60000);

            httpURLConnection.connect();
            //POST请求
            DataOutputStream out = new DataOutputStream(httpURLConnection.getOutputStream());

            String jsonString = JSONObject.toJSONString(postDTO);
            log.info("getFightInfo()方法发送的json数据为：{}", jsonString);
            out.writeBytes(new String(jsonString.getBytes(), StandardCharsets.UTF_8));
            out.flush();
            out.close();
            BufferedReader reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), "utf-8"));
            String _line;
            while ((_line = reader.readLine()) != null) {
                lineBuffer.append(_line);
            }

            log.info("接收到的原始JSON数据：{}", lineBuffer);
            if (StringUtils.isEmpty(lineBuffer.toString())) {
                log.error("返回数据为空，直接返回null");
                return null;
            }
            Map<String, Object> map = JSONObject.parseObject(lineBuffer.toString(), new TypeReference<Map<String, Object>>() {
            });

            if (map.get("code") != null && (int) map.get("code") != 200) {
                log.error("战斗服务器返回数据失败");
                return null;
            }

            return map;

        } catch (IOException e) {
            e.printStackTrace();
            log.error("具体的错误信息：", e);
            throw new BusinessException("传输错误", -1);
        }

    }


    public static Map<String, Object> getAiInfo(String url, Object postDTO) {
        HttpURLConnection httpURLConnection = null;

        StringBuilder lineBuffer = new StringBuilder();
        try {

            URL _url = new URL(url);
            httpURLConnection = HttpUtil.getHttpConnection(_url);
            httpURLConnection.setConnectTimeout(60000);
            httpURLConnection.setReadTimeout(60000);

            httpURLConnection.connect();
            //POST请求
            DataOutputStream out = new DataOutputStream(httpURLConnection.getOutputStream());

            String jsonString = JSONObject.toJSONString(postDTO);
            log.info("getAiInfo()方法发送的json数据为：{}", jsonString);
            out.writeBytes(new String(jsonString.getBytes()));
            out.flush();
            out.close();
            BufferedReader reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), "utf-8"));
            String _line;
            while ((_line = reader.readLine()) != null) {
                lineBuffer.append(_line);
            }

            log.info("接收到的原始JSON数据：{}", lineBuffer);
            if (StringUtils.isEmpty(lineBuffer.toString())) {
                log.error("返回数据为空，直接返回null");
                return null;
            }
            Map<String, Object> map = JSONObject.parseObject(lineBuffer.toString(), new TypeReference<Map<String, Object>>() {
            });

            if (map.get("code") != null && (int) map.get("code") != 200) {
                log.error("ai数据查找失败");
                return null;
            }

            return map;

        } catch (IOException e) {
            e.printStackTrace();
            log.error("具体的错误信息：", e);
            throw new BusinessException("传输错误", -1);
        }

    }

    public static Map<String, Object> getMouldData(String url, Object postDTO) {
        HttpURLConnection httpURLConnection = null;

        StringBuilder lineBuffer = new StringBuilder();
        try {

            URL _url = new URL(url);
            httpURLConnection = HttpUtil.getHttpConnection(_url);
            httpURLConnection.setConnectTimeout(60000);
            httpURLConnection.setReadTimeout(60000);

            httpURLConnection.connect();
            //POST请求
            DataOutputStream out = new DataOutputStream(httpURLConnection.getOutputStream());

            String jsonString = JSONObject.toJSONString(postDTO);
            log.info("getAiInfo()方法发送的json数据为：{}", jsonString);
            out.writeBytes(new String(jsonString.getBytes()));
            out.flush();
            out.close();
            BufferedReader reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), "utf-8"));
            String _line;
            while ((_line = reader.readLine()) != null) {
                lineBuffer.append(_line);
            }

            log.info("接收到的原始JSON数据：{}", lineBuffer);
            if (StringUtils.isEmpty(lineBuffer.toString())) {
                log.error("返回数据为空，直接返回null");
                return null;
            }
            Map<String, Object> map = JSONObject.parseObject(lineBuffer.toString(), new TypeReference<Map<String, Object>>() {
            });

            // if (map.get("code") != null && (int)map.get("code") != 200){
            //     log.error("ai数据查找失败");
            //     return null;
            // }

            return map;

        } catch (IOException e) {
            e.printStackTrace();
            log.error("具体的错误信息：", e);
            throw new BusinessException("传输错误", -1);
        }

    }


    /**
     * 发送get请求
     *
     * @param url
     * @return
     */
    public static String getString(String url) {

        StringBuilder lineBuffer = new StringBuilder();
        try {
            HttpURLConnection httpURLConnection = HttpUtil.HttpConnectionByGet(new URL(url));
            httpURLConnection.setConnectTimeout(60000);
            httpURLConnection.setReadTimeout(60000);

            httpURLConnection.connect();
            BufferedReader reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), "utf-8"));
            String _line;
            while ((_line = reader.readLine()) != null) {
                lineBuffer.append(_line);
            }

            return lineBuffer.toString();
        } catch (Exception e) {
            throw new BusinessException("传输错误", -1);
        }
    }
}
