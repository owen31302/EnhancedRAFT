package signedMethods;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.UnsupportedEncodingException;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * Created by shan on 5/26/17.
 */
public class Keys {
    private RSAPrivateKey privateKey;
    private RSAPublicKey publicKey;

    public Keys() {
        KeyPairGenerator keyPairGen = null;
        try {
            keyPairGen = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        // init key size to be 1024
        keyPairGen.initialize(512);

        // generate key pair
        KeyPair keyPair = keyPairGen.generateKeyPair();
        // get private key
        this.privateKey = (RSAPrivateKey) keyPair.getPrivate();
        // get public key
        this.publicKey = (RSAPublicKey) keyPair.getPublic();
    }

    public RSAPublicKey getPublicKey() {
        return this.publicKey;
    }

    public RSAPrivateKey getPrivateKey() {
        return this.privateKey;
    }


    public static String encrypt(Key k, String data) {
        byte[] data_bytes = new byte[0];
        try {
            data_bytes = data.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if (k != null) {

            Cipher cipher = null;
            try {
                cipher = Cipher.getInstance("RSA");
                cipher.init(Cipher.ENCRYPT_MODE, k);
                byte[] resultBytes = cipher.doFinal(data_bytes);
                return new String(resultBytes);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            } catch (BadPaddingException e) {
                e.printStackTrace();
            } catch (IllegalBlockSizeException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    public static String decrypt(Key k, String data) {
        byte[] data_bytes = new byte[0];
        try {
            data_bytes = data.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if (k != null) {

            Cipher cipher = null;
            try {
                cipher = Cipher.getInstance("RSA");
                cipher.init(Cipher.DECRYPT_MODE, k);
                byte[] resultBytes = cipher.doFinal(data_bytes);
                return new String(resultBytes);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            } catch (BadPaddingException e) {
                e.printStackTrace();
            } catch (IllegalBlockSizeException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

//    public static byte[] handleData(Key k, byte[] data, int encrypt) {
//
//        if (k != null) {
//
//            Cipher cipher = null;
//            try {
//                cipher = Cipher.getInstance("RSA");
//                if (encrypt == 1) {
//                    cipher.init(Cipher.ENCRYPT_MODE, k);
//                    byte[] resultBytes = cipher.doFinal(data);
//                    return resultBytes;
//                } else if (encrypt == 0) {
//                    cipher.init(Cipher.DECRYPT_MODE, k);
//                    byte[] resultBytes = cipher.doFinal(data);
//                    return resultBytes;
//                } else {
//                    System.out.println("参数必须为: 1 加密 0解密");
//                }
//            } catch (NoSuchAlgorithmException e) {
//                e.printStackTrace();
//            } catch (NoSuchPaddingException e) {
//                e.printStackTrace();
//            }
//
//
//        }
//        return null;
//    }
}
