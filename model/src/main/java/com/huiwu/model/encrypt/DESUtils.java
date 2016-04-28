package com.huiwu.model.encrypt;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

public class DESUtils {

    public static byte[] encrypt(byte[] dataSource, String password) {
        try {
            SecureRandom random = new SecureRandom();
            DESKeySpec desKey = new DESKeySpec(password.getBytes());
            //TODO　创建一个密匙工厂，然后用它把DESKeySpec转换成
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey secureKey = keyFactory.generateSecret(desKey);
            //TODO　Cipher对象实际完成加密操作
            Cipher cipher = Cipher.getInstance("DES");
            //TODO　用密匙初始化Cipher对象
            cipher.init(Cipher.ENCRYPT_MODE, secureKey, random);
            //TODO　现在，获取数据并加密
            //TODO　正式执行加密操作
            return cipher.doFinal(dataSource);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] decrypt(byte[] src, String password) throws Exception {
        //TODO　 DES算法要求有一个可信任的随机数源
        SecureRandom random = new SecureRandom();
        //TODO　 创建一个DESKeySpec对象
        DESKeySpec desKey = new DESKeySpec(password.getBytes());
        //TODO　 创建一个密匙工厂
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        //TODO　 将DESKeySpec对象转换成SecretKey对象
        SecretKey secureKey = keyFactory.generateSecret(desKey);
        //TODO　 Cipher对象实际完成解密操作
        Cipher cipher = Cipher.getInstance("DES");
        //TODO　 用密匙初始化Cipher对象
        cipher.init(Cipher.DECRYPT_MODE, secureKey, random);
        //TODO　 真正开始解密操作
        return cipher.doFinal(src);
    }
}
