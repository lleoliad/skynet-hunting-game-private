package org.skynet.service.provider.hunting.obsolete.pay;

import org.skynet.service.provider.hunting.obsolete.common.util.CommonUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;


@Slf4j
public class NettyHttpServerHandler {


    /*-1类型为默认不加密*/
    /**
     * url加密
     */
    private static final int ENCRYPT_TYPE_1 = 1;
    /**
     * 奇偶拆分加密
     */
    private static final int ENCRYPT_TYPE_2 = 2;
    /**
     * 字母偏移加密
     */
    private static final int ENCRYPT_TYPE_3 = 3;
    /**
     * 倒序加字母偏移
     */
    private static final int ENCRYPT_TYPE_4 = 4;
    /**
     * 倒序加字母大小写转换
     */
    private static final int ENCRYPT_TYPE_5 = 5;
    /**
     * 首尾交叉加密
     */
    private static final int ENCRYPT_TYPE_6 = 6;
    /**
     * 矩阵S型加密
     */
    private static final int ENCRYPT_TYPE_7 = 7;

    private static Properties properties = null;
    private static Properties psStateProperties = null;
    private static Properties ipWhitelistProperties = null;
    private static Properties accountEmailList = null;
    private static String regionIpWhitelist = null;

    /*记录监控时间段内各包的充值次数*/
    private static Map<String, AtomicLong> payCount = new ConcurrentHashMap<>();

    public static Properties getAccountEmailListProperties() {
        if (accountEmailList == null) {
            synchronized (NettyHttpServerHandler.class) {
                if (accountEmailList == null) {
                    try {
                        accountEmailList = CommonUtils.readProperties("accountEmail.properties");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return accountEmailList;
    }

}
