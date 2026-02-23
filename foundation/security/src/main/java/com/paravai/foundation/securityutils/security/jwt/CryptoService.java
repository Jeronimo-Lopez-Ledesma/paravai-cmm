package com.paravai.foundation.securityutils.security.jwt;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;

public class CryptoService {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";

    public static String getKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
        keyGen.init(256);
        SecretKey key = keyGen.generateKey();
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    public static Key string2Key (String keyString){
        byte[] keyDecoded = Base64.getDecoder().decode(keyString);
        return new SecretKeySpec(keyDecoded, 0, keyDecoded.length, ALGORITHM);
    }

    public static String encrypt (String plaintext, String keyStr) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        Key key = string2Key(keyStr);

        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
        byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

        byte[] ivAndEncrypted = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, ivAndEncrypted, 0, iv.length);
        System.arraycopy(encrypted, 0, ivAndEncrypted, iv.length, encrypted.length);

        return Base64.getEncoder().encodeToString(ivAndEncrypted);
    }

    public static String decipher (String cipherText, String keyStr) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Key key = string2Key(keyStr);
        byte[] ivAndEncrypted = Base64.getDecoder().decode(cipherText);

        byte[] iv = new byte[16];
        byte[] encrypted = new byte[ivAndEncrypted.length - 16];
        System.arraycopy(ivAndEncrypted, 0, iv, 0, 16);
        System.arraycopy(ivAndEncrypted, 16, encrypted, 0, encrypted.length);

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
        byte[] decrypted = cipher.doFinal(encrypted);

        return new String(decrypted, StandardCharsets.UTF_8);
    }

    public static String hmac (String plaintext, String secret) {
        try {
            byte[] bytes = plaintext.getBytes(StandardCharsets.UTF_8);
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256Hmac.init(secretKey);
            byte[] hash = sha256Hmac.doFinal(bytes);
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }
}