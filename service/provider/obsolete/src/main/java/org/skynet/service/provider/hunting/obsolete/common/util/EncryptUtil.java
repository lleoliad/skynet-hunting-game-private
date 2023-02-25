package org.skynet.service.provider.hunting.obsolete.common.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EncryptUtil {

    /**
     * 矩阵S型加密
     */
    public static String arrayBendingDecode(String value, int length) {
        char[] valueChars = value.toCharArray();
        char[] chars = new char[value.length()];
        int arrayCount = valueChars.length / length;
        int index = 0;
        for (int i = 0; i < arrayCount; i++) {
            for (int j = length; j > 0; j--) {
                int x;
                if (j % 2 != length % 2) {
                    x = j * arrayCount - (arrayCount - i - 1);
                } else {
                    x = j * arrayCount - i;
                }
                if (valueChars[x - 1] == 0) {
                    continue;
                }
                chars[index++] = valueChars[x - 1];
            }
        }
        return new String(chars).trim();
    }

    /**
     * 矩阵S型加密
     */
    public static String arrayBendingEncrypt(String value, int length) {
        char[] valueChars = value.toCharArray();
        int arrayLength = value.length() + length - (value.length() % length);
        char[] chars = new char[arrayLength];
        int index = 0;
        for (int i = 0; i < length; i++) {
            for (int j = arrayLength / length; j > 0; j--) {
                int x;
                if ((length - i) % 2 != length % 2) {
                    x = (arrayLength / length - j + 1) * length - i;
                } else {
                    x = j * length - i;
                }
                chars[index++] = x <= valueChars.length ? valueChars[x - 1] : ' ';
            }
        }
        return new String(chars);
    }

    /**
     * 首尾交叉加密
     */
    public static String startAndEndCrossDecode(String value) {
        char[] valueChars = value.toCharArray();
        char[] chars = new char[value.length()];
        char[] descChars = new char[value.length() / 2 + 1];//+1防止单数长度不够
        int ascCount = 0;
        int descCount = 0;
        for (int i = 0; i < value.length(); i++) {
            if (i % 2 == 0) {
                chars[ascCount++] = valueChars[i];
            } else {
                descChars[descCount++] = valueChars[i];
            }
        }
        for (int i = 0; i < descChars.length; i++) {
            if (descChars[descChars.length - 1 - i] == 0) {
                continue;
            }
            chars[ascCount++] = descChars[descChars.length - 1 - i];
        }
        return new String(chars);
    }

    /**
     * 首尾交叉加密
     */
    public static String startAndEndCrossEncrypt(String value) {
        char[] valueChars = value.toCharArray();
        char[] chars = new char[value.length()];
        int ascCount = 0;
        int descCount = 0;
        for (int i = 0; i < value.length(); i++) {
            if (i % 2 == 0) {
                chars[i] = valueChars[ascCount];
                ascCount++;
            } else {
                chars[i] = valueChars[value.length() - 1 - descCount];
                descCount++;
            }
        }
        return new String(chars);
    }

    /**
     * 倒序加字母大小写转换
     */
    public static String reverseLetterCaseDecode(String value) {
        char[] valueChars = value.toCharArray();
        char[] chars = new char[value.length()];
        for (int i = 0; i < value.length(); i++) {
            chars[i] = letterCase(valueChars[value.length() - 1 - i]);
        }
        return new String(chars);
    }

    /**
     * 倒序加字母大小写转换
     */
    public static String reverseLetterCaseEncrypt(String value) {
        char[] valueChars = value.toCharArray();
        char[] chars = new char[value.length()];
        for (int i = 0; i < value.length(); i++) {
            chars[i] = letterCase(valueChars[value.length() - 1 - i]);
        }
        return new String(chars);
    }

    private static char letterCase(char valueChar) {
        if (valueChar >= 65 && valueChar <= 90) {
            return (char) (valueChar + 32);
        } else if (valueChar >= 97 && valueChar <= 122) {
            return (char) (valueChar - 32);
        }
        return valueChar;
    }

    /**
     * 倒序加字母偏移
     */
    public static String reverseLetterOffsetDecode(String value) {
        char[] valueChars = value.toCharArray();
        char[] chars = new char[value.length()];
        for (int i = 0; i < value.length(); i++) {
            chars[i] = valueChars[value.length() - 1 - i];
        }
        StringBuilder result = new StringBuilder(value.length());
        for (char letter : chars) {
            if ((letter >= 'a' && letter <= 'z') || (letter >= 'A' && letter <= 'Z')) {
                char c = (char) (letter - 2);
                if (letter == 'b') {
                    c = 'z';
                } else if (letter == 'B') {
                    c = 'Z';
                } else if (letter == 'a') {
                    c = 'y';
                } else if (letter == 'A') {
                    c = 'Y';
                }
                result.append(c);
            } else {
                result.append(letter);
            }

        }
        return result.toString();
    }

    /**
     * 倒序加字母偏移
     */
    public static String reverseLetterOffsetEncrypt(String value) {
        char[] valueChars = value.toCharArray();
        char[] chars = new char[value.length()];
        for (int i = 0; i < value.length(); i++) {
            chars[i] = valueChars[value.length() - 1 - i];
        }
        StringBuilder result = new StringBuilder(value.length());
        for (char letter : chars) {
            if ((letter >= 'a' && letter <= 'z') || (letter >= 'A' && letter <= 'Z')) {
                char c = (char) (letter + 2);
                if (letter == 'z') {
                    c = 'b';
                } else if (letter == 'Z') {
                    c = 'B';
                } else if (letter == 'y') {
                    c = 'a';
                } else if (letter == 'Y') {
                    c = 'A';
                }
                result.append(c);
            } else {
                result.append(letter);
            }

        }
        return result.toString();
    }

    /**
     * 字母偏移解密
     */
    public static String letterOffsetDecode(String value) {
        char[] chars = value.toCharArray();
        StringBuilder result = new StringBuilder(value.length());
        for (char letter : chars) {
            if ((letter >= 'a' && letter <= 'z') || (letter >= 'A' && letter <= 'Z')) {
                char c = (char) (letter - 1);
                if (letter == 'a') {
                    c = 'z';
                } else if (letter == 'A') {
                    c = 'Z';
                }
                result.append(c);
            } else {
                result.append(letter);
            }

        }
        return result.toString();
    }

    /**
     * 字母偏移加密
     */
    public static String letterOffsetEncrypt(String value) {
        char[] chars = value.toCharArray();
        StringBuilder result = new StringBuilder(value.length());
        for (char letter : chars) {
            if ((letter >= 'a' && letter <= 'z') || (letter >= 'A' && letter <= 'Z')) {
                char c = (char) (letter + 1);
                if (letter == 'z') {
                    c = 'a';
                } else if (letter == 'Z') {
                    c = 'A';
                }
                result.append(c);
            } else {
                result.append(letter);
            }

        }
        return result.toString();
    }

    /**
     * 奇偶解密 lua语言第一位为1，java为0，故奇偶相反
     */
    public static String parityDecode(String value, String propertiesValue) {
        String[] split = value.split(propertiesValue);
        StringBuilder result = new StringBuilder(value.length());
        int length = split[0].length() + split[1].length();
        int oddIndex = split[1].length() - 1;
        int evenIndex = split[0].length() - 1;
        for (int i = 0; i < length; i++) {
            if (i % 2 == 1) {
                result.append(split[1].charAt(oddIndex--));
            } else {
                result.append(split[0].charAt(evenIndex--));
            }
        }
        return result.toString();
    }

    /**
     * 奇偶加密
     */
    public static String parityEncrypt(String value, String propertiesValue) {
        int length = value.length() / 2;
        List<Character> odd = new ArrayList<>(length);//奇数
        List<Character> even = new ArrayList<>(length);//偶数
        for (int i = 0; i < value.length(); i++) {
            if (i % 2 == 1) {
                odd.add(value.charAt(i));
            } else {
                even.add(value.charAt(i));
            }
        }
        StringBuilder result = new StringBuilder(value.length() + propertiesValue.length());
        Collections.reverse(even);
        even.stream().forEach(e -> result.append(e));
        result.append(propertiesValue);
        Collections.reverse(odd);
        odd.stream().forEach(e -> result.append(e));
        return result.toString();
    }

    public static void main(String[] args) {
    }

}
