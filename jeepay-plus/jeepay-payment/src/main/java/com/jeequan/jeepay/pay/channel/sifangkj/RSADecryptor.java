package com.jeequan.jeepay.pay.channel.sifangkj;

import javax.crypto.Cipher;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

public class RSADecryptor {

    // 私钥对象
    private PrivateKey privateKey;

    public RSADecryptor(String privateKeyPem) throws Exception {
        // 解析PEM格式的私钥
        String pkcs8EncodedPrivateKey = privateKeyPem.replace("-----BEGIN PRIVATE KEY-----\n", "")
                .replace("-----END PRIVATE KEY-----", "").replaceAll("\\s", "");
        byte[] encoded = Base64.getDecoder().decode(pkcs8EncodedPrivateKey);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        this.privateKey = kf.generatePrivate(keySpec);
    }

    // 分段解密函数
    public String decrypt(String encryptedMessage) throws Exception {
        String[] segments = encryptedMessage.split("\\|");
        StringBuilder decryptedTextBuilder = new StringBuilder();
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");

        for (String segment : segments) {
            byte[] encryptedBytes = Base64.getDecoder().decode(segment);

            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decryptedChunk = cipher.doFinal(encryptedBytes);

            decryptedTextBuilder.append(new String(decryptedChunk));
        }

        return decryptedTextBuilder.toString();
    }
}