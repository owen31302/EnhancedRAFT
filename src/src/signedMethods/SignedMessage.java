package signedMethods;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

/**
 * Created by shan on 5/26/17.
 */

public class SignedMessage implements Serializable {
    private String messageType;
    private byte[] encryptedMessageContent;

    public SignedMessage(String messageType, String message, Key key) {
        this.messageType = messageType;
        this.encryptedMessageContent = encrypt(key, message);
    }

    public static byte[] encrypt(Key k, String data) {
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
                return resultBytes;
//                return new String(resultBytes);
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
//            catch (UnsupportedEncodingException e) {
//                e.printStackTrace();
//            }
        }
        return null;
    }

    /**
     *
     * @param k
     * @param data_bytes
     * @return
     */
    public static String decrypt(Key k, byte[] data_bytes) {
        if (k != null) {

            Cipher cipher = null;
            try {
                cipher = Cipher.getInstance("RSA");
                cipher.init(Cipher.DECRYPT_MODE, k);
                byte[] resultBytes = cipher.doFinal(data_bytes);
                return new String(resultBytes, "UTF-8");
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
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    public String getMessageType() {
        return messageType;
    }

    public byte[] getEncryptedMessageContent() {
        return encryptedMessageContent;
    }

}
