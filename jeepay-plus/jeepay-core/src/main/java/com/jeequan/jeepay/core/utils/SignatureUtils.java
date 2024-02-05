package com.jeequan.jeepay.core.utils;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

public class SignatureUtils {

    public static String md5(String str) {
        return DigestUtils.md5Hex(str);
    }

    public static void main(String[] args) {

    }

    /**
     * hmac-sha256签名算法
     *
     * @param map 待加密参数
     * @param key 密钥
     * @return 签名字符串
     */
    public static String generateSign(Map<String, Object> map, String key) {
        try {
            StringJoiner sj = new StringJoiner("&");
            map.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(x -> {
                sj.add(x.getKey()+"="+x.getValue());
            });
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            byte[] array = sha256_HMAC.doFinal(sj.toString().getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte item : array) {
                sb.append(Integer.toHexString((item & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static String getSignContent(Map<String, Object> param) {
        SortedMap<String,Object> map = new TreeMap(param);
        String str = "";
        for (Map.Entry<String,Object> entry : map.entrySet()){
            if (!Objects.isNull(entry.getValue()) &&
                    StringUtils.isNotEmpty(entry.getValue().toString()))
                str += entry.getValue();
        }
        return str;
    }

    public static String getSignContent(Map<String, Object> param, String eqChar, String joinChar, String[] ignoreKeys, Object[] ignoreVal, boolean valueEncode) {
        StringJoiner joiner = new StringJoiner(joinChar);
        SortedMap<String, Object> sort = new TreeMap<>(param);
        Set<Map.Entry<String, Object>> es = sort.entrySet();
        Iterator<Map.Entry<String, Object>> it = es.iterator();

        while (it.hasNext()) {
            Map.Entry entry = it.next();
            String k = (String) entry.getKey();
            Object v = entry.getValue();
            if (valueEncode)
                try {
                    v = URLEncoder.encode(entry.getValue().toString(), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            boolean hasIgnoreKey = (ignoreKeys != null && Arrays.<String>asList(ignoreKeys).contains(k));
            boolean hasIgnoreVal = (ignoreVal != null && Arrays.<Object>asList(ignoreVal).contains(v));
            if (!hasIgnoreKey && !hasIgnoreVal)
                joiner.add(k + eqChar + v);
        }
        return joiner.toString();
    }

    public static String getSignContentValue(Map<String, Object> param, String[] ignoreKeys, Object[] ignoreVal, boolean valueEncode) {
        StringJoiner joiner = new StringJoiner("");
        SortedMap<String, Object> sort = new TreeMap<>(param);
        Set<Map.Entry<String, Object>> es = sort.entrySet();
        Iterator<Map.Entry<String, Object>> it = es.iterator();

        while (it.hasNext()) {
            Map.Entry entry = it.next();
            String k = (String) entry.getKey();
            Object v = entry.getValue();
            if (valueEncode)
                try {
                    v = URLEncoder.encode(entry.getValue().toString(), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            boolean hasIgnoreKey = (ignoreKeys != null && Arrays.<String>asList(ignoreKeys).contains(k));
            boolean hasIgnoreVal = (ignoreVal != null && Arrays.<Object>asList(ignoreVal).contains(v));
            if (!hasIgnoreKey && !hasIgnoreVal)
                joiner.add(v + "");
        }
        return joiner.toString();
    }

    public static String getSignContent(Map<String, Object> param, String[] ignoreKeys, Object[] ignoreVal) {
        return getSignContent(param, "=", "&", ignoreKeys, ignoreVal, false);
    }

    public static String getSignContentFilterEmpty(Map<String, Object> param, String[] ignoreKeys) {
        return getSignContent(param, "=", "&", ignoreKeys, new String[]{null, ""}, false);
    }

    public static String getSignContentValEncode(Map<String, Object> param, String[] ignoreKeys, Object[] ignoreVal) {
        return getSignContent(param, "=", "&", ignoreKeys, ignoreVal, true);
    }

    /**
     * key升序排列
     *
     * @param map
     * @return
     */
    public static Map<String, Object> sortMapByKey(Map<String, Object> map) {
        if (map == null || map.isEmpty())
            return null;
        Map<String, Object> sortMap = new TreeMap<>((Comparator<? super String>) new MapKeyComparator());
        sortMap.putAll(map);
        return sortMap;
    }

    /**
     * 降序排列
     *
     * @param map
     * @return
     */
    public static Map<String, Object> sortDescMapByKey(Map<String, Object> map) {
        if (map == null || map.isEmpty())
            return null;
        Map<String, Object> sortMap = new TreeMap<>((Comparator<? super String>) new MapKeyDescComparator());
        sortMap.putAll(map);
        return sortMap;
    }


    public static String getSignContentDesc(Map<String, Object> param, String[] ignoreKeys, Object[] ignoreVal) {
        return getSignContentDesc(param, "=", "&", ignoreKeys, ignoreVal, false);
    }

    public static String getSignContentValEncodeDesc(Map<String, Object> param, String[] ignoreKeys, Object[] ignoreVal) {
        return getSignContentDesc(param, "=", "&", ignoreKeys, ignoreVal, true);
    }

    public static String getSignContentDesc(Map<String, Object> param, String eqChar, String joinChar, String[] ignoreKeys, Object[] ignoreVal, boolean valueEncode) {
        StringJoiner joiner = new StringJoiner(joinChar);

        Map<String, Object> sortedMap = sortDescMapByKey(param);
        Iterator<Map.Entry<String, Object>> it = sortedMap.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry entry = it.next();
            String k = (String) entry.getKey();
            Object v = entry.getValue();
            if (valueEncode)
                try {
                    v = URLEncoder.encode(entry.getValue().toString(), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            boolean hasIgnoreKey = (ignoreKeys != null && Arrays.<String>asList(ignoreKeys).contains(k));
            boolean hasIgnoreVal = (ignoreVal != null && Arrays.<Object>asList(ignoreVal).contains(v));
            if (!hasIgnoreKey && !hasIgnoreVal)
                joiner.add(k + eqChar + v);
        }
        return joiner.toString();
    }

    /** RSA SHA1算法使用私钥对数据生成数字签名【私钥签名】
     * @param data 待加密数据
     * @param privKey  私钥
     * @return String 加密数据
     * @throws Exception
     */
    public static String buildRSASHA1SignByPrivateKey(String data, String privKey) {
        try {
            PrivateKey privateKey = (PrivateKey) KeyFactory.getInstance("RSA")
                    .generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privKey)));
            Signature signature = Signature.getInstance("SHA1WithRSA");
            signature.initSign(privateKey);
            signature.update(data.getBytes());
            return Base64.getEncoder().encodeToString(signature.sign());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /** RSA SHA1算法使用公钥校验数字签名【公钥验证】
     * @param data 待加密数据
     * @param rsaData  待验签的加密数据
     * @param pubKey  公钥
     * @return boolean 验签是否成功一致
     * @throws Exception
     */
    public static boolean buildRSASHA1VerifyByPublicKey(String data, String rsaData, String pubKey) {
        try {
            PublicKey publicKey = (PublicKey) KeyFactory.getInstance("RSA")
                    .generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(pubKey)));
            Signature signature = Signature.getInstance("SHA1WithRSA");
            signature.initVerify(publicKey);
            signature.update(data.getBytes());
            return signature.verify(Base64.getDecoder().decode(rsaData));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * SHA1WithRSA算法使用私钥对数据生成数字签名
     *
     * @param signValue 待签名的明文字符串(MD5后字符串)
     * @param key       RSA商户私钥字符串
     * @return RSA私钥签名后的经过Base64编码的字符串
     */
    public static String buildSHA1WithRSASignByPrivateKey(String signValue, String key) {
        try {
            //通过PKCS#8编码的Key指令获得私钥对象
            PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(key));
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = keyFactory.generatePrivate(pkcs8KeySpec);
            Signature signature = Signature.getInstance("SHA1WithRSA");
            signature.initSign(privateKey);
            signature.update(signValue.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(signature.sign());
        } catch (Exception e) {
            throw new RuntimeException("签名字符串[" + signValue + "]时遇到异常", e);
        }
    }

    /**
     * SHA1WithRSA算法使用公钥校验数字签名
     *
     * @param signValue 参与签名的明文字符串(MD5后字符串)
     * @param key       RSA平台公钥字符串(平台公钥，通道管理->对接参数中查看)
     * @param sign      回调参数sign值
     * @return true--验签通过,false--验签未通过
     */
    public static boolean buildSHA1WithRSAVerifyByPublicKey(String signValue, String key, String sign) {
        try {
            //通过X509编码的Key指令获得公钥对象
            X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(key));
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(x509KeySpec);
            Signature signature = Signature.getInstance("SHA1WithRSA");
            signature.initVerify(publicKey);
            signature.update(signValue.getBytes("UTF-8"));
            return signature.verify(Base64.getDecoder().decode(sign));
        } catch (Exception e) {
            throw new RuntimeException("验签字符串[" + signValue + "]时遇到异常", e);
        }
    }

    /**
     * SHA256WithRSA算法使用私钥对数据生成数字签名
     *
     * @param signValue 待签名的明文字符串(MD5后字符串)
     * @param key       RSA私钥字符串
     * @return RSA私钥签名后的经过Base64编码的字符串
     */
    public static String buildSHA256WithRSASignByPrivateKey(String signValue, String key) {
        try {
            //通过PKCS#8编码的Key指令获得私钥对象
            PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(key));
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = keyFactory.generatePrivate(pkcs8KeySpec);
            Signature signature = Signature.getInstance("SHA256WithRSA");
            signature.initSign(privateKey);
            signature.update(signValue.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(signature.sign());
        } catch (Exception e) {
            throw new RuntimeException("签名字符串[" + signValue + "]时遇到异常", e);
        }
    }

    /**
     * SHA256WithRSA算法使用公钥校验数字签名
     *
     * @param signValue 参与签名的明文字符串(MD5后字符串)
     * @param key       RSA公钥字符串(平台公钥，通道管理->对接参数中查看)
     * @param sign      回调参数sign值
     * @return true--验签通过,false--验签未通过
     */
    public static boolean buildSHA256WithRSAVerifyByPublicKey(String signValue, String key, String sign) {
        try {
            //通过X509编码的Key指令获得公钥对象
            X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(key));
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(x509KeySpec);
            Signature signature = Signature.getInstance("SHA256WithRSA");
            signature.initVerify(publicKey);
            signature.update(signValue.getBytes("UTF-8"));
            return signature.verify(Base64.getDecoder().decode(sign));
        } catch (Exception e) {
            throw new RuntimeException("验签字符串[" + signValue + "]时遇到异常", e);
        }
    }

    /**
     * AES解密
     * @param data 密文
     * @param key  密钥，长度16
     * @param iv   偏移量，长度16
     * @return 明文
     * @author miracle.qu
     */
    public static String decryptAES(String data, String key, String iv) {
        try {
            data = replaceAllSpecial(data);
            byte[] encrypted1 = Base64.getDecoder().decode(data);

            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            SecretKeySpec keyspec = new SecretKeySpec(key.getBytes(), "AES");
            if (iv != null) {
                IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes());
                cipher.init(Cipher.DECRYPT_MODE, keyspec, ivspec);
            } else {
                cipher.init(Cipher.DECRYPT_MODE, keyspec);
            }


            byte[] original = cipher.doFinal(encrypted1);
            String originalString = new String(original);
            return originalString.trim();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * AES加密
     *
     * @param data 加密参数
     * @param key  密钥
     * @param iv   偏移量
     * @return
     * @throws Exception
     */
    public static String encryptAES(String data, String key, String iv) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            int blockSize = cipher.getBlockSize();
            data = replaceAllSpecial(data);
            byte[] dataBytes = data.getBytes();
            int plaintextLength = dataBytes.length;

            if (plaintextLength % blockSize != 0) {
                plaintextLength = plaintextLength + (blockSize - (plaintextLength % blockSize));
            }

            byte[] plaintext = new byte[plaintextLength];
            System.arraycopy(dataBytes, 0, plaintext, 0, dataBytes.length);


            SecretKeySpec keyspec = new SecretKeySpec(key.getBytes(), "AES");
            if (iv != null) {
                IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes());
                cipher.init(Cipher.ENCRYPT_MODE, keyspec, ivspec);
            } else {
                cipher.init(Cipher.ENCRYPT_MODE, keyspec);
            }

            byte[] encrypted = cipher.doFinal(plaintext);
            return Base64.getEncoder().encodeToString(encrypted);

        } catch (Exception e) {
            return null;
        }
    }


    public static String replaceAllSpecial(String s) {
        return s.replaceAll("\\\\", "");
    }
}
