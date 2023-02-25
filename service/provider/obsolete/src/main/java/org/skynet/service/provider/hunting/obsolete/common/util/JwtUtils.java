package org.skynet.service.provider.hunting.obsolete.common.util;


import org.skynet.service.provider.hunting.obsolete.common.exception.BusinessException;
import org.skynet.service.provider.hunting.obsolete.common.result.ResponseEnum;
import io.jsonwebtoken.*;
import org.springframework.util.StringUtils;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.Key;
import java.util.Date;

/**
 * token工具
 */
public class JwtUtils {

    private static long tokenExpiration = 3 * 24 * 60 * 60 * 1000;
    private static String tokenSignKey = "@ZW!@d0Mq5!av0u5nf59&nR8sO8lE$";

    private static Key getKeyInstance() {
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS384;
        byte[] bytes = DatatypeConverter.parseBase64Binary(tokenSignKey);
        return new SecretKeySpec(bytes, signatureAlgorithm.getJcaName());
    }

    public static String createToken(String userUid) {
        String token = Jwts.builder()
                .setExpiration(new Date(System.currentTimeMillis() + tokenExpiration))
                .signWith(SignatureAlgorithm.HS384, getKeyInstance())
                .claim("userUid", userUid)
                .compressWith(CompressionCodecs.GZIP)
                .compact();
        return token;
    }

    /**
     * 判断token是否有效
     *
     * @param token
     * @return false or true
     */
    public static boolean checkToken(String token) {
        if (StringUtils.isEmpty(token)) {
            return false;
        }
        try {
            Jwts.parser().setSigningKey(getKeyInstance()).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 给用户一个1分钟过期的token
     *
     * @param userUid
     * @return
     */
    public static String removeToken(String userUid) {

        String newToken = Jwts.builder()
                .setExpiration(new Date(System.currentTimeMillis() + 60 * 1000))
                .signWith(SignatureAlgorithm.HS384, getKeyInstance())
                .claim("userUid", userUid)
                .compressWith(CompressionCodecs.GZIP)
                .compact();
        return newToken;
    }

    /**
     * 校验token并返回Claims
     *
     * @param token
     * @return
     */
    private static Claims getClaims(String token) {
        if (StringUtils.isEmpty(token)) {
            throw new BusinessException("玩家上传的token 是 null");
        }
        try {
            Jws<Claims> claimsJws = Jwts.parser().setSigningKey(getKeyInstance()).parseClaimsJws(token);
            Claims claims = claimsJws.getBody();
            return claims;
        } catch (Exception e) {
            throw new BusinessException(ResponseEnum.LOGIN_AUTH_ERROR);
        }
    }

    /**
     * 获取token中的userUid
     *
     * @param token
     * @return
     */
    public static String getUserUid(String token) {
        if (StringUtils.isEmpty(token)) {
            throw new BusinessException("玩家上传的token 是 null");
        }
        Claims claims = getClaims(token);
        String userUid = (String) claims.get("userUid");
        return userUid;
    }
}

