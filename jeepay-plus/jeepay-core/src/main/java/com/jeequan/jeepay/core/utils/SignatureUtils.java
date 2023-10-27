package com.jeequan.jeepay.core.utils;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

public class SignatureUtils {

    public static String md5(String str) {
        return DigestUtils.md5Hex(str);
    }

    public static void main(String[] args) {

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
     * RSA算法使用私钥对数据生成数字签名
     *
     * @param signValue 待签名的明文字符串(MD5后字符串)
     * @param key       RSA私钥字符串
     * @return RSA私钥签名后的经过Base64编码的字符串
     */
    public static String buildRSASignByPrivateKey(String signValue, String key) {
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
     * RSA算法使用公钥校验数字签名
     *
     * @param signValue 参与签名的明文字符串(MD5后字符串)
     * @param key       RSA公钥字符串(平台公钥，通道管理->对接参数中查看)
     * @param sign      参数sign值
     * @return true--验签通过,false--验签未通过
     */
    public static boolean buildRSAVerifyByPublicKey(String signValue, String key, String sign) {
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
}
