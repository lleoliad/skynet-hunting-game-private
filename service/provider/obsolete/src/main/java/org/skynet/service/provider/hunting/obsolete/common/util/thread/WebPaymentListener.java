//package com.cn.huntingrivalserver.common.util.thread;
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONObject;
//
//import com.cn.huntingrivalserver.common.util.CommonUtils;
//import com.cn.huntingrivalserver.common.util.HttpUtil;
//import com.cn.huntingrivalserver.common.util.TimeUtils;
//import com.cn.huntingrivalserver.pay.NettyHttpServer;
//import io.netty.util.internal.StringUtil;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.http.HttpResponse;
//import org.apache.http.client.HttpClient;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.entity.StringEntity;
//import org.apache.http.impl.client.HttpClients;
//import org.apache.http.util.EntityUtils;
//import org.springframework.util.StringUtils;
//
//
//import java.io.IOException;
//import java.util.*;
//
///**
// * web支付监控
// **/
//@Slf4j
//public class WebPaymentListener implements Runnable {
//
//    private final static long INTERVAL_TIME = 5 * 60 * 1000L;
//
//    /**
//     * 切换web支付，区间时间内无订单信息
//     */
//    private final static int NO_WEB_ORDERS = 0;
//
//    /**
//     * 切换web支付，区间时间内有订单信息，结束报送
//     */
//    private final static int YES_WEB_ORDERS = 1;
//
//    /**
//     * 切换web支付，区间时间内有订单信息，结束报送
//     */
//    private final static int LINK_ERROR = 2;
//
//    private long lastPrintTime = 0L;
//
//    /**
//     * 监控服查看订单存在入口
//     */
//    private final static String GET_WEB_STATE = "/topUpOrder/getWebStateByPackageName";
//
//
//    /**
//     * 回传信息
//     */
//    private final static String RETURN_MSG = "msg";
//
//    /**
//     * 回传码
//     */
//    private final static String RETURN_CODE = "code";
//
//    /**
//     * 回传值
//     */
//    private final static String RETURN_DATA = "data";
//
//    /**
//     * 切换web支付，区间时间内有订单信息，结束报送
//     */
//    private final static String comma = ",";
//
//    @Override
//    public void run() {
//        /*读取配置信息*/
//        JSONObject tempJson = CommonUtils.readMonitorConfig();
//        long curTime = System.currentTimeMillis();
//        if (curTime - lastPrintTime >= INTERVAL_TIME) {
//            lastPrintTime = curTime;
//            log.info("webMonitor alive......");
//        }
//        if (NettyHttpServer.monitorJson == null) {
//            NettyHttpServer.monitorJson = tempJson;
//        } else {
//            if (!tempJson.equals(NettyHttpServer.monitorJson)) {
//                NettyHttpServer.monitorJson = tempJson;
//                log.info("下包监控配置文件发生变化, 已经更新");
//            }
//        }
//
//        String monitorPackageNames = CommonUtils.readFile(CommonUtils.configPath, "monitorWeb.txt");
//
//        if (StringUtils.isEmpty(monitorPackageNames)) {
//            log.info("monitorWeb.txt 监控配置为空，不对包体进行监控！");
//            return;
//        }
//
//        if (StringUtils.isEmpty(tempJson.getString("monitor_server"))) {
//            log.info("监控服地址为空，请更新监控服地址");
//            return;
//        }
//
//        Set<Map.Entry<String, Object>> entries = NettyHttpServer.configJson.entrySet();//所有包的配置信息
//
//        String[] appNames = monitorPackageNames.split(comma);//包名
//        long startTime = System.currentTimeMillis() - tempJson.getLongValue("web_polling_time") * 1000;
//        /*请求监控服*/
//        for (String appName : appNames) {
//            String monitorAppName = "monitor_app_name";
//            /*获取包名称*/
//            for (Map.Entry<String, Object> entry : entries) {
//                JSONObject value = JSON.parseObject(entry.getValue().toString());
//                if (value.getString("package_name").equals(appName)) {
//                    monitorAppName = value.getString("monitor_app_name");
//                }
//            }
//
//            JSONObject requestJSON = new JSONObject();
//            requestJSON.put("startTime", startTime);
//            requestJSON.put("appPackageName", appName);
//            /*请求监控服*/
//            String returnMsg = HttpUtil.postJson(tempJson.getString("monitor_server") + GET_WEB_STATE, requestJSON.toString(), "utf-8");
//            JSONObject returnJSON = JSON.parseObject(returnMsg);
//            /*处理结果*/
//            if (StringUtil.isNullOrEmpty(returnMsg) || returnJSON.getString(RETURN_DATA) == null) {//访问失败
//                log.info("监控服访问失败，请更新监控服地址数据");
//                JSONObject linkMessage = getMonitorLinkMessage(LINK_ERROR, monitorAppName, startTime, tempJson.getString("ding_ding_at_mobiles"));
//                postMessage(tempJson.getString("dingding_url"), linkMessage.toJSONString());
//            } else if (returnJSON.getBoolean(RETURN_DATA)) {//有返回值且为有数据
//                /*退出循环*/
//                CommonUtils.writeFile(CommonUtils.configPath, "monitor.txt", CommonUtils.removeString(CommonUtils.readFile(CommonUtils.configPath, "monitor.txt"), appName, comma).toString(), false, false);
//                CommonUtils.writeFile(CommonUtils.configPath, "monitorWeb.txt", CommonUtils.removeString(CommonUtils.readFile(CommonUtils.configPath, "monitorWeb.txt"), appName, comma).toString(), false, false);
//                JSONObject linkMessage = getMonitorLinkMessage(YES_WEB_ORDERS, monitorAppName, startTime, tempJson.getString("ding_ding_at_mobiles"));
//                postMessage(tempJson.getString("dingding_url"), linkMessage.toJSONString());
//            } else {//无数据，继续监控
//                JSONObject linkMessage = getMonitorLinkMessage(NO_WEB_ORDERS, monitorAppName, startTime, tempJson.getString("ding_ding_at_mobiles"));
//                postMessage(tempJson.getString("dingding_url"), linkMessage.toJSONString());
//            }
//        }
//
//    }
//
//
//    private JSONObject getMonitorLinkMessage(int monitorListenerType, String monitorName, long starTime, String mobiles) {
//        JSONObject bodys = new JSONObject();
//        bodys.put("msgtype", "text");
//        JSONObject textJSON = new JSONObject();
//        long currentTime = System.currentTimeMillis();//系统当前时间
//        long periodTime = currentTime - starTime;//间隔频率
//        StringBuilder text = new StringBuilder();
//        if (monitorListenerType == LINK_ERROR) {
//            text.append("（红色）");
//            text.append("\n");
//            text.append("监控服务器访问失败");
//            text.append("\n");
//            text.append("时间区间：【" + TimeUtils.getFormatTimeString(new Date(starTime)) + "~" + TimeUtils.getFormatTimeString(new Date(currentTime)) + "】");
//            text.append("\n");
//            text.append("包："+monitorName +"访问监控服失败，请及时更新监控服地址数据,或手动关闭该包监控");
//        } else if (monitorListenerType == NO_WEB_ORDERS) {
//            text.append("（红色）");
//            text.append("\n");
//            text.append("98提审服务器：【" + monitorName + "】");
//            text.append("\n");
//            text.append("时间区间：【" + TimeUtils.getFormatTimeString(new Date(starTime)) + "~" + TimeUtils.getFormatTimeString(new Date(currentTime)) + "】");
//            text.append("\n");
//            text.append(monitorName + "包在" + periodTime / (60 * 1000) + "分内无Web充值数据，请检查Web支付通道是否畅通。");
//        } else if (monitorListenerType == YES_WEB_ORDERS) {
//            text.append("98提审服务器：【" + monitorName + "】");
//            text.append("\n");
//            text.append(monitorName + "包在切换Web支付后，已经具有Web充值数据，现已关闭该包监控。");
//        }
//        textJSON.put("content", text.toString());
//        bodys.put("text", textJSON);
//        /*拼上@功能*/
//        JSONObject atUsersJson = new JSONObject();
//        String[] split = mobiles.split(",");
//        List<String> mobileList = new ArrayList<>(Arrays.asList(split));
//        atUsersJson.put("atMobiles", mobileList);
//        atUsersJson.put("isAtAll", false);
//        bodys.put("at", atUsersJson);
//        return bodys;
//    }
//
//    private static String postMessage(String url, String message) {
//        String responseEntity = "";
//        try {
//            System.out.println("postMessage request:" + message); //打印响应消息实体
//            HttpPost httpPost = new HttpPost(url);
//            httpPost.addHeader("Content-Type", "application/json; charset=utf-8");
//            StringEntity se = new StringEntity(message, "utf-8");
//            httpPost.setEntity(se);
//            HttpClient httpClient = HttpClients.createDefault();
//            HttpResponse response = httpClient.execute(httpPost);
//            responseEntity = EntityUtils.toString(response.getEntity());
//            System.out.println("callback response:" + responseEntity); //打印响应消息实体
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return responseEntity;
//    }
//}
