//package com.cn.huntingrivalserver.common.util.thread;
//
//import com.alibaba.fastjson.JSONObject;
//
//import com.cn.huntingrivalserver.common.util.CommonUtils;
//import com.cn.huntingrivalserver.pay.NettyHttpServer;
//import lombok.extern.slf4j.Slf4j;
//
//
//import java.util.Date;
//
///**
// * @program: PS_Server
// * @Date: 2020/10/15 10:44
// * @Author: SEVEN
// * @Description:
// */
//@Slf4j
//public class TimeThread extends Thread {
//
//
//
//    @Override
//    public void run() {
//        while (true) {
//            try {
//                sleep(5 * 60 * 1000);
//                log.info("current time is " + new Date() + " server running...");
//
//                JSONObject tempJson = CommonUtils.readConfig();
//                if (!tempJson.equals(NettyHttpServer.configJson)) {
//                    NettyHttpServer.configJson = tempJson;
//                    log.info("项目配置文件发生变化, 已经更新");
//                }
//            } catch (Throwable t) {
//                log.error(CommonUtils.getStringExceptionStack(t));
//            }
//        }
//    }
//}
