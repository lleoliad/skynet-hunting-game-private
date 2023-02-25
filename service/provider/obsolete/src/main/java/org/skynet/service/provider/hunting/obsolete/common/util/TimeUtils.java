package org.skynet.service.provider.hunting.obsolete.common.util;


import org.skynet.service.provider.hunting.obsolete.common.util.thread.ThreadLocalUtil;
import org.skynet.service.provider.hunting.obsolete.config.GameConfig;
import org.skynet.service.provider.hunting.obsolete.pojo.environment.GameEnvironment;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 时间相关内容工具
 */
@Slf4j
public class TimeUtils {

    /**
     * @return 现在的unix时间(秒)
     */
//    public static Long getUnixTimeSecond(Long serverTimeOffset){
//        return (long) Math.floor(System.currentTimeMillis() / 1000D)+ serverTimeOffset;
//    }
    public static Long getUnixTimeSecond() {
        Long serverTimeOffset = ThreadLocalUtil.get();
        return (long) Math.floor(System.currentTimeMillis() / 1000D) + serverTimeOffset;
    }

    private static SimpleDateFormat simpledateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static SimpleDateFormat endTheDay = new SimpleDateFormat("yyyy-MM-dd");

    private static SimpleDateFormat endTheMinute = new SimpleDateFormat("yyyy-MM-dd");


    /**
     * 获取现在的unix时间(毫秒)
     *
     * @return
     */
    public static Long getUnixTimeMilliseconds() {
        return System.currentTimeMillis() + GameEnvironment.serverTimeOffset * 1000;
    }

    /**
     * 获取现在unix时间天
     *
     * @return
     */
    public static Long getUnixTimeDay() {
        return (long) Math.floor(getUnixTimeMilliseconds() * 1.0 / (24 * 3600));
    }

    /**
     * 获取现在unix分钟
     *
     * @return
     */
    public static Long getUnixTimeMinute() {

        return (long) Math.floor(getUnixTimeMilliseconds() * 1.0 / 60);
    }

    /**
     * 取现在完整时间字符串
     *
     * @return
     */
    public static String getNowFormatTimeString() {
        Date date = new Date();
        return simpledateformat.format(date);
    }

    /**
     * 转换完整的时间字符串
     *
     * @param date
     * @return
     */
    public static String getUnixTimeMinuteFormat(Date date) {
        return simpledateformat.format(date);
    }

    /**
     * 以yyyy-mm-dd输出日期
     *
     * @return
     */
    public static String getUnixTimeDayFormat() {
        Date date = new Date();
        return endTheDay.format(date);
    }

    /**
     * 以yyyy-mm-dd-hh-mm输出日期
     *
     * @return
     */
    public static String getUnixTimeMinuteFormat() {
        Date date = new Date();
        return endTheMinute.format(date);
    }

    /**
     * 获得标准时间
     * 标准时间根据纽约时区来计算，用于计算一些事件的开始时间
     *
     * @return
     */
    public static Long getStandardTimeSecond() {
        return getUnixTimeSecond() + GameConfig.standardTimeZoneOffset;
    }

    /**
     * 获得标准时间下的天数
     *
     * @return
     */
    public static Long getStandardTimeDay() {

        return (long) Math.floor(getStandardTimeSecond() * 1.0 / (24 * 3600));
    }

    public static Long standardTimeSecondToUnixTimeSecond(long standardTimeSecond) {
        return standardTimeSecond - GameConfig.standardTimeZoneOffset;
    }

    /**
     * 计算从现在到指定标准日的时间差（秒）
     *
     * @param targetStandardDay
     * @return
     */
    public static Long getSecondsFromNowToTargetStandardDay(long targetStandardDay) {
        return getSpanSecondsFromUnixSecondToTargetStandardDay(getUnixTimeSecond(), targetStandardDay);
    }

    /**
     * 计算Unix时间（秒）到某个标准日的时间跨度（秒）
     *
     * @param unixTimeSecond
     * @param targetStandardDay
     * @return
     */
    public static Long getSpanSecondsFromUnixSecondToTargetStandardDay(long unixTimeSecond, long targetStandardDay) {

        long targetStandardDayUnixTimeSecond = targetStandardDay * 24 * 3600 - GameConfig.standardTimeZoneOffset;
        return targetStandardDayUnixTimeSecond - unixTimeSecond;
    }

    /**
     * 将unix时间转为标准日
     *
     * @param unixTimeSecond
     * @return
     */
    public static Long convertUnixTimeSecondToStandardDay(long unixTimeSecond) {

        long standardTimeSecond = unixTimeSecond + GameConfig.standardTimeZoneOffset;
        return (long) Math.floor(standardTimeSecond * 1.0 / (24 * 3600));
    }


    /**
     * @return 现在的unix时间(秒)
     */
    public static Long getUnixTimeNow() {
        return (long) Math.floor(System.currentTimeMillis() / 1000D);
    }

}
