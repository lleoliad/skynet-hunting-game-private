package org.skynet.service.provider.hunting.obsolete.common.util;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.google.common.base.Charsets;
import com.google.common.primitives.Bytes;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.skynet.components.hunting.user.dao.entity.UserData;
import org.skynet.service.provider.hunting.obsolete.common.exception.BusinessException;
import org.skynet.service.provider.hunting.obsolete.pojo.dto.DownloadDataDTO;
import org.springframework.util.StringUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
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

    /**
     * 发送get请求
     *
     * @param url
     * @return
     */
    public static InputStream getInputStream(String url) {

        try {
            HttpURLConnection httpURLConnection = HttpUtil.HttpConnectionByGet(new URL(url));

            httpURLConnection.connect();

            return httpURLConnection.getInputStream();
        } catch (Exception e) {
            throw new BusinessException("传输错误", -1);
        }
    }

    public static void main(String[] args) {
        try {
            String mySignature = "Ln7lH0papLSJATgVW4TLN+t8wN+fe5tOx5YimF124i6CZt5hB9BbNdr8Ja6hgZtNs891fayVHPOk5QMdTrH0UlZ8e9fA/4wXYdpjXjuskPvbmrsSqKMRQJTrRxm8epnz0o+DTnpLQptXKnwddH3tVIfz7HsG648w625jYyo/E7cqrQEFxZG9h1xCc36iKheEsToJjuUyD3maH2om8uwUTXa1HBeI8JBQs89OytF3rCBEtZLGNNFpcb6qEKZp4lm7Al/PNoCxt7NnRMhnrzF9QVb9Yub89QOjM8EDLhuOKjldJlj2MgxRZC4fnuwZDmlIi06+T2HFpO5G8Dj1hCwTzab4H5ZFFrWzJl6QO7re6SpTEF20vTLKqZeg3Undjvmff1WH/chreB/lBFIubwL4HwD4CwibAe2r2uVqs5TeQVOahe/nPYyLb7lTaYwAkhi2A0ZpTK72clkkh1LiymA5JLU5/l1wXklEH6DKNbOfb+o4yg0Cfn4yLZ2QBk6hhgliUoXR1+HF1f2s4fC14+biv1xTkYmyrE7CKmKa9QCNmMRqt9IlkPvE3q8Li1FjDgrBlij1pbh+qRWkyCalUSxGWcxviL2B4Chvl3a2uvu426X1rEbIaugyIOySaWvXo1sWf+xC5COi9s0+yQjJTzZpcSP7FPlfwzth6mD4HbN6OAw=";
//            mySignature = mySignature.replace("+", "%2B").replace("\t", "").replace(" ", "+").replace("+=+", " = ");
//            mySignature = URLDecoder.decode(mySignature, "utf-8");
            String salt = "WYS69Q==";
//            salt = salt.replace("+", "%2B").replace("\t", "").replace(" ", "+").replace("+=+", " = ");
//            salt = URLDecoder.decode(salt, "utf-8");
            String publicKeyUrl = "https://static.gc.apple.com/public-key/gc-prod-8.cer";
            long timeStamp = 1679126717109l;
            String bundleId = "com.huntingfly.huntingsniper";
            String playerId = "T:_121a9f5ab67486b086518fe7257a0cf2";

            byte[] signBytes = Base64.decodeBase64(mySignature);

            byte[] buffer = ByteBuffer.allocate(Long.BYTES).putLong(timeStamp).array();
//            byte[] buffer = byteBuffer.array();

//            byte[] buffer = new byte[8];
//            for (int i = 0; i < 8; i++) {
//                buffer[7 - i] = (byte)(timeStamp & 0xff);
//                timeStamp = timeStamp >> 8;
//            }

            byte[] saltBytes = Base64.decodeBase64(salt);
            byte[] content = Bytes.concat(playerId.getBytes(Charsets.UTF_8), bundleId.getBytes(Charsets.UTF_8), buffer, saltBytes);
//            byte[] content = String.format("%s%s%s%s", playerId, bundleId, timeStamp, salt).getBytes(StandardCharsets.UTF_8);

//            MessageDigest digest = MessageDigest.getInstance("SHA-256");
//            digest.update(content);
//            content = digest.digest();

            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) new URL(publicKeyUrl).openConnection();
//                connection.setRequestMethod("GET");

            } catch (IOException e) {
                System.err.println("连接错误");
            } finally {
                if (null != connection) {
                    connection.disconnect();
                }
            }

            connection.connect();

            InputStream publicKeyInputStream = connection.getInputStream();

//            String publicKey = HttpUtil.getString(publicKeyUrl);
//            final byte[] keyBytes = IoUtil.readBytes(publicKeyInputStream);

            boolean result = false;
            try {
                CertificateFactory cf = CertificateFactory.getInstance("x.509");
                X509Certificate cer = (X509Certificate) cf.generateCertificate(publicKeyInputStream);
                cer.checkValidity();
                PublicKey publicKey = cer.getPublicKey();
                Signature signature = Signature.getInstance(cer.getSigAlgName());
                signature.initVerify(publicKey);
                signature.update(content);
                result = signature.verify(signBytes);

                if (!result) {
                    System.out.println("gamecenter登陆失败" + result);
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("gamecenter登陆失败" + result);
            }
            System.out.println(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
