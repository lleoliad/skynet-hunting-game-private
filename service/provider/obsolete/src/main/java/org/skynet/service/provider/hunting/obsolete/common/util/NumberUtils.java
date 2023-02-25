package org.skynet.service.provider.hunting.obsolete.common.util;

import java.util.List;

/**
 * 数字工具
 */
public class NumberUtils {

    public static Integer randomInt(Double min, Double max) {

        long round = Math.round(min);
        return (int) (Math.floor(Math.random() * (max - min)) + min);
    }

    public static Double randomFloat(Double min, Double max) {

        return Math.random() * (max - min) + min;
    }

    public static Object randomElementInArray(List list) {

        if (list.size() == 0) {
            return null;
        }

        int randomIndex = (int) Math.floor(Math.random() * list.size());

        return list.get(randomIndex);
    }

    public static Integer lerp(Double a, Double b, Double t) {

        return (int) ((b - a) * t + a);
    }

    /**
     * 打乱排序
     *
     * @param list
     * @return
     */
    public static void shuffleArray(List list, Integer startShuffleIndex) {

        if (list.size() <= 0) {
            return;
        }
        if (startShuffleIndex >= list.size()) {
            return;
        }
        for (int i = startShuffleIndex; i < list.size(); i++) {

            int randomIndex = randomInt(i * 1.0, list.size() * 1.0);
            Object temp = list.get(randomIndex);
            list.set(randomIndex, list.get(i));
            list.set(i, temp);
        }
    }
}
