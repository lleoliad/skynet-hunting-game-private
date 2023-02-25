package org.skynet.service.provider.hunting.obsolete.common.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * 压缩和解压工具
 */
public class DeflaterUtils {

    /**
     * 压缩
     *
     * @param unzip
     * @return
     */
    public static String zipString(String unzip) {

        Deflater deflater = new Deflater(9); // 0 ~ 9 压缩等级 低到高

        deflater.setInput(unzip.getBytes());

        deflater.finish();

        final byte[] bytes = new byte[1024];

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(1024);

        while (!deflater.finished()) {

            int length = deflater.deflate(bytes);

            outputStream.write(bytes, 0, length);

        }

        deflater.end();

        return new sun.misc.BASE64Encoder().encodeBuffer(outputStream.toByteArray());

    }

    /**
     * 解压
     *
     * @param zip
     * @return
     * @throws IOException
     */
    public static String unzipString(String zip) throws IOException {

        byte[] decode = new sun.misc.BASE64Decoder().decodeBuffer(zip);

        Inflater inflater = new Inflater();

        inflater.setInput(decode);

        final byte[] bytes = new byte[1024];

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(1024);

        try {

            while (!inflater.finished()) {

                int length = inflater.inflate(bytes);

                outputStream.write(bytes, 0, length);

            }

        } catch (DataFormatException e) {

            e.printStackTrace();

            return null;

        } finally {

            inflater.end();

        }

        return outputStream.toString();

    }


}
