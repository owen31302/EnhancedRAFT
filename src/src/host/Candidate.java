package host;

import Communicator.TCP_Communicator;
import signedMethods.Keys;
import signedMethods.SignedMessage;


import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * Created by owen on 5/26/17.
 */
public class Candidate implements Runnable{
    private double _time;
    private final int BASELATENCY = 300;
    private final int DURATION = 200;
    private boolean _closed = false;

    @Override
    public void run() {
        String msg = "~O(∩_∩)O哈哈~";
        String enc = "UTF-8";
        Keys lucy = new Keys();
        RSAPublicKey pubkey_lucy = lucy.getPublicKey();
        RSAPrivateKey prikey_lucy = lucy.getPrivateKey();

        byte[] result = SignedMessage.encrypt(prikey_lucy, msg);

        Keys keyPair = new Keys();
        RSAPrivateKey privateKey;
        RSAPublicKey publicKey;
        publicKey = keyPair.getPublicKey();
        privateKey = keyPair.getPrivateKey(); // !!!  to do : send my public key to all other hosts

        SignedMessage signedMessage = new SignedMessage("Q", "Q", privateKey);

        //TCP_Communicator tcp_communicator = new TCP_Communicator();
    }
}
