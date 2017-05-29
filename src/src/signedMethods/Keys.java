package signedMethods;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
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

}
