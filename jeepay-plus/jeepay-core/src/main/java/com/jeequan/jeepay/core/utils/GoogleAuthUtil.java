package com.jeequan.jeepay.core.utils;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * 谷歌验证工具类
 */
public class GoogleAuthUtil {
    public static String GenerateSecretKey() {
        GoogleAuthenticator gAuth = new GoogleAuthenticator();
// 生成秘钥
        GoogleAuthenticatorKey key = gAuth.createCredentials();
// 将秘钥保存在数据库或与用户关联
        String secretKey = key.getKey();
        return secretKey;
    }


    public static String GetQRCodeStr(String userName, String secret) {
        String format = "otpauth://totp/%s?secret=%s";
        return String.format(format, new Object[]{userName, secret});
    }

    /**
     * 校验谷歌验证码
     *
     * @param secret
     * @param code
     * @return
     */
    public static boolean CheckCode(String secret, int code) {
        // 创建GoogleAuthenticator实例
        GoogleAuthenticator gAuth = new GoogleAuthenticator();
        // 验证生成的验证码是否有效
        boolean isCodeValid = gAuth.authorize(secret, code);
        return isCodeValid;
    }

}
