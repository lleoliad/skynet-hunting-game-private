//package com.cn.huntingrivalserver.common.util.thread;
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONObject;
//
//import com.cn.huntingrivalserver.common.util.CommonUtils;
//import com.cn.huntingrivalserver.common.util.HttpUtil;
//import com.cn.huntingrivalserver.common.util.TimeUtils;
//import com.cn.huntingrivalserver.pay.NettyHttpServer;
//import com.cn.huntingrivalserver.pay.NettyHttpServerHandler;
//
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.lang3.StringUtils;
//import org.apache.http.HttpResponse;
//import org.apache.http.client.HttpClient;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.entity.StringEntity;
//import org.apache.http.impl.client.HttpClients;
//import org.apache.http.util.EntityUtils;
//
//
//import java.io.IOException;
//import java.util.*;
//
///**
// * @Author: fxia003
// * @Date: 2022/05/23 14:40
// * @Description:
// */
//@Slf4j
//public class MonitorListener implements Runnable{
//
//    private final static long INTERVAL_TIME = 5 * 60 * 1000L;
//
//    /**无充值并且不进行商店校验*/
//    private final static int NO_RECHARGE = 1;
//    /**无充值但是商店校验成功*/
//    private final static int NO_RECHARGE_AND_YES_STORE = 2;
//    /**无充值且商店校验失败*/
//    private final static int NO_RECHARGE_AND_NO_STORE = 3;
//    /**无充值且商店校验失败，不自动切换WEB支付*/
//    private final static int NO_RECHARGE_AND_NO_WEB = 4;
//    /**无充值且商店校验失败，自动切换WEB支付*/
//    private final static int NO_RECHARGE_AND_YES_WEB = 5;
//
//    private long lastPrintTime = 0L;
//
//
//
//    @Override
//    public void run() {
//        JSONObject tempJson = CommonUtils.readMonitorConfig();
//        long curTime = System.currentTimeMillis();
//        if(curTime - lastPrintTime >= INTERVAL_TIME) {
//            lastPrintTime = curTime;
//            log.info("monitor alive......");
//        }
//        if(NettyHttpServer.monitorJson == null) {
//            NettyHttpServer.monitorJson = tempJson;
//        } else {
//            if(!tempJson.equals(NettyHttpServer.monitorJson)) {
//                NettyHttpServer.monitorJson = tempJson;
//                log.info("下包监控配置文件发生变化, 已经更新");
//            }
//        }
//
//        Map<String,String> monitorMap = new HashMap<>();
//        String monitorPackageNames = CommonUtils.readFile(CommonUtils.configPath, "monitor.txt");
//
//        if (StringUtils.isEmpty(monitorPackageNames)) {
//            log.info("monitor.txt 监控配置为空，不对包体进行监控！");
//            return;
//        }
//
//        Set<Map.Entry<String, Object>> entries = NettyHttpServer.configJson.entrySet();
//        for (Map.Entry<String, Object> entry: entries) {
//            int monitorListenerType = 0;
//            JSONObject value = JSON.parseObject(entry.getValue().toString());
//            String packageName = value.getString("package_name");
//            /*判断包体是否开启监控*/
//            if (!monitorPackageNames.contains(packageName)){
//                log.info(packageName + "不开启监控！");
//                continue;
//            }
//            /*判断包体是否有充值*/
//            long payCount = NettyHttpServerHandler.getPayCount(packageName);
//            if (payCount > 0) {
//                continue;
//            } else {
//                /*没有充值需要进行报警*/
//                monitorListenerType = NO_RECHARGE;
//            }
//            /*判断商店页是否存在*/
//            String storeUrl = value.getString("store_url");
//            /*老包不配置谷歌商店链接默认不开启商店校验*/
//            if(StringUtils.isNotEmpty(storeUrl)){
////                String gameName = value.getString("game_name");
////                String checkName = Util.isNotEmpty(gameName) ? gameName : packageName;
//                String storeResult = HttpUtil.sendGet(storeUrl);
//                /*保持原有校验逻辑*/
//                if(storeResult.contains(packageName)){
//                    monitorListenerType = NO_RECHARGE_AND_YES_STORE;
//                } else {
//                    monitorListenerType = NO_RECHARGE_AND_NO_STORE;
//                }
//            }
//            String monitorName = value.getString("monitor_app_name") == null ? packageName : value.getString("monitor_app_name");
//            monitorMap.put(packageName,monitorListenerType + "," + monitorName);
//        }
//
//        Set<Map.Entry<String, String>> monitorPackages = monitorMap.entrySet();
//        for (Map.Entry<String, String> entry:monitorPackages) {
//            String[] value = entry.getValue().split(",");
//            int monitorListenerType = Integer.parseInt(value[0]);
//            String monitorName = value[1];
//            int pollingTime = tempJson.getIntValue("polling_time");
//            /*这里的推迟链接会推迟这个提审服的所有监控 故取消推迟功能*/
//            if (monitorListenerType < NO_RECHARGE_AND_NO_STORE) {
//                /*这里直接发送钉钉报警*/
//                JSONObject linkMessage = getMonitorLinkMessage(monitorListenerType, monitorName,pollingTime);
//                postMessage(tempJson.getString("dingding_url"),linkMessage.toJSONString());
//            }else {
//                /*根据主控修改web支付返回的状态进行钉钉报警*/
//                String code = postMessage(tempJson.getString("main_url"), entry.getKey());
//                if (Objects.equals(code,"0")) {
//                    /*自动切换web支付成功*/
//                    JSONObject linkMessage = getMonitorLinkMessage(NO_RECHARGE_AND_YES_WEB, monitorName,pollingTime);
//                    postMessage(tempJson.getString("dingding_url"),linkMessage.toJSONString());
//                    /*写入包体*/
//                    String packageNames = CommonUtils.readFile(CommonUtils.configPath, "monitorWeb.txt") == null ?CommonUtils.readFile(CommonUtils.configPath, "monitorWeb.txt"):"";
//                    if(!packageNames.contains(monitorName)){
//                        packageNames += "," + monitorName;
//                        CommonUtils.writeFile(CommonUtils.configPath, "monitorWeb.txt", entry.getKey(), false, false);
//                    }
//                }else {
//                    /*自动切换web支付失败*/
//                    JSONObject linkMessage = getMonitorLinkMessage(NO_RECHARGE_AND_NO_WEB, monitorName,pollingTime);
//                    postMessage(tempJson.getString("dingding_url"),linkMessage.toJSONString());
//                }
//            }
//        }
//        NettyHttpServerHandler.clearPayCount();
//    }
//
//    private JSONObject getMonitorLinkMessage(int monitorListenerType,String monitorName,int periodTime) {
//        JSONObject bodys = new JSONObject();
//        bodys.put("msgtype", "text");
//        JSONObject textJSON = new JSONObject();
//        long currentTime = System.currentTimeMillis();
//        long lastTime = currentTime - periodTime * 1000;
//        StringBuilder text = new StringBuilder();
//        if (monitorListenerType == NO_RECHARGE) {
//            text.append("（黄色）");
//            text.append("\n");
//            text.append("98提审服务器：【" + monitorName + "】");
//            text.append("\n");
//            text.append("时间区间：【" + TimeUtils.getFormatTimeString(new Date(lastTime)) + "~" +  TimeUtils.getFormatTimeString(new Date(currentTime)) + "】");
//            text.append("\n");
//            text.append(monitorName + "包在" + periodTime/60 + "min内无谷歌充值数据，请手动核实商店页链接");
//        }else if (monitorListenerType == NO_RECHARGE_AND_YES_STORE) {
//            text.append("（蓝色）");
//            text.append("\n");
//            text.append("98提审服务器：【" + monitorName + "】");
//            text.append("\n");
//            text.append("时间区间：【" + TimeUtils.getFormatTimeString(new Date(lastTime)) + "~" +  TimeUtils.getFormatTimeString(new Date(currentTime)) + "】");
//            text.append("\n");
//            text.append(monitorName + "包在" + periodTime/60 + "min内无谷歌充值数据，商店页链接可以访问");
//        }else if (monitorListenerType == NO_RECHARGE_AND_NO_WEB) {
//            text.append("（紫色）");
//            text.append("\n");
//            text.append("98提审服务器：【" + monitorName + "】");
//            text.append("\n");
//            text.append("时间区间：【" + TimeUtils.getFormatTimeString(new Date(lastTime)) + "~" +  TimeUtils.getFormatTimeString(new Date(currentTime)) + "】");
//            text.append("\n");
//            text.append(monitorName + "包在" + periodTime/60 + "min内无谷歌充值数据，商店页链接无法访问，不会自动切换WEB，请核实实际情况手动更改");
//        }else if (monitorListenerType == NO_RECHARGE_AND_YES_WEB) {
//            text.append("（红色）");
//            text.append("\n");
//            text.append("98提审服务器：【" + monitorName + "】");
//            text.append("\n");
//            text.append("时间区间：【" + TimeUtils.getFormatTimeString(new Date(lastTime)) + "~" +  TimeUtils.getFormatTimeString(new Date(currentTime)) + "】");
//            text.append("\n");
//            text.append(monitorName + "包在" + periodTime/60 + "min内无谷歌充值数据，商店页链接无法访问，已自动切换WEB支付，请核实实际情况");
//        }
//        textJSON.put("content",text.toString());
//        bodys.put("text", textJSON);
//        return bodys;
//    }
//
//    private static String postMessage(String url,String message) {
//        String responseEntity = "";
//        try {
//            System.out.println("postMessage request:" + message); //打印响应消息实体
//            HttpPost httpPost = new HttpPost(url);
//            httpPost.addHeader("Content-Type", "application/json; charset=utf-8");
//            StringEntity se = new StringEntity(message,"utf-8");
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
//
//}
