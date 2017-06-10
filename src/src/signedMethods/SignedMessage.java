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


    /**
     * Constructor
     * @param messageType
     * @param message
     * @param key it should be a private key to encrypt
     */
    public SignedMessage(String messageType, String message, Key key) {
        this.messageType = messageType;
        this.encryptedMessageContent = encrypt(key, message);
    }

    /**
     * Encrypt a message with String type using private key
     * @param k
     * @param data
     * @return
     */
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

    /**
     * Decrypt message using public key
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

    /**
     * Get message type
     * @return
     */
    public String getMessageType() {
        return messageType;
    }

    /**
     * Get encrypted message content
     * @return
     */
    public byte[] getEncryptedMessageContent() {
        return encryptedMessageContent;
    }

    /**
     * Get plan text
     * @param k
     * @return
     */
    public String getPlanText(Key k) {
        return decrypt(k, encryptedMessageContent);
    }
}
