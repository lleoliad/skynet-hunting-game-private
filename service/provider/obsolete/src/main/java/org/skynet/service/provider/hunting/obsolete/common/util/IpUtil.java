package org.skynet.service.provider.hunting.obsolete.common.util;

import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServerHttpRequest;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class IpUtil {
    public static String getIpAddr(HttpServletRequest request) {
        String ipAddress = null;
        try {
            //X-Forwarded-For：Squid 服务代理
            ipAddress = request.getHeader("x-forwarded-for");
            if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                //Proxy-Client-IP：apache 服务代理
                ipAddress = request.getHeader("Proxy-Client-IP");
            }
            if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                //WL-Proxy-Client-IP：weblogic 服务代理
                ipAddress = request.getHeader("WL-Proxy-Client-IP");
            }
            if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                //HTTP_CLIENT_IP：有些代理服务器
                ipAddress = request.getHeader("HTTP_CLIENT_IP");
            }
            if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getHeader("HTTP_X_FORWARDED_FOR");
            }
            if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                //X-Real-IP：nginx服务代理
                ipAddress = request.getHeader("X-Real-IP");
            }
            if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getRemoteAddr();
                if (ipAddress.equals("127.0.0.1")) {
                    // 根据网卡取本机配置的IP
                    InetAddress inet = null;
                    try {
                        inet = InetAddress.getLocalHost();
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }
                    ipAddress = inet.getHostAddress();
                }
            }
            // 对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
            if (ipAddress != null && ipAddress.length() > 15) { // "***.***.***.***".length()
                // = 15
                if (ipAddress.indexOf(",") > 0) {
                    ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
                }
            }
        } catch (Exception e) {
            ipAddress="";
        }
        // ipAddress = this.getRequest().getRemoteAddr();

        return ipAddress;
    }

    /**
     * 获取客户端IP
     * @param request
     * @return
     */
    public static String getIpAddress(ServerHttpRequest request) {
        // HttpHeaders headers = request.getHeaders();
        // String ip = headers.getFirst("x-forwarded-for");
        // if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
        //     // 多次反向代理后会有多个ip值，第一个ip才是真实ip
        //     if (ip.indexOf(",") != -1) {
        //         ip = ip.split(",")[0];
        //     }
        // }
        // if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
        //     ip = headers.getFirst("Proxy-Client-IP");
        // }
        // if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
        //     ip = headers.getFirst("WL-Proxy-Client-IP");
        // }
        // if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
        //     ip = headers.getFirst("HTTP_CLIENT_IP");
        // }
        // if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
        //     ip = headers.getFirst("HTTP_X_FORWARDED_FOR");
        // }
        // if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
        //     ip = headers.getFirst("X-Real-IP");
        // }
        String ip = getIpAddress(request.getHeaders());
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddress().getAddress().getHostAddress();
        }
        return ip;
    }

    public static String getIpAddress(HttpHeaders headers) {
        String ip = headers.getFirst("x-forwarded-for");
        if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
            // 多次反向代理后会有多个ip值，第一个ip才是真实ip
            if (ip.indexOf(",") != -1) {
                ip = ip.split(",")[0];
            }
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = headers.getFirst("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = headers.getFirst("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = headers.getFirst("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = headers.getFirst("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = headers.getFirst("X-Real-IP");
        }
        // if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
        //     ip = request.getRemoteAddress().getAddress().getHostAddress();
        // }
        return ip;
    }
}
