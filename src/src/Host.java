import signedMethods.Keys;

import java.io.IOException;
import java.net.ServerSocket;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;

/**
 * Created by TC_Yeh on 5/26/2017.
 */
public class Host {
    private StateManager stateManager;
    //need to store public keys of other hosts
    private ServerSocket aServer;
    private Leader leader;
    private Follower follower;
    private Candidate candidate;
    private RSAPrivateKey privateKey;
    private RSAPublicKey publicKey;
    private HostAddress myAddress;   // !!! to store my name, my ip, my port, my public key
    private int term;

    public Host(String hostName) throws IOException {
        stateManager = new StateManager();
        leader = new Leader();
        follower = new Follower();
        candidate = new Candidate();
        term = 0;
        aServer = new ServerSocket(0);
        myAddress = new HostAddress(hostName, aServer.getInetAddress().getHostAddress(), aServer.getLocalPort());
        Keys keyPair = new Keys();
        this.publicKey = keyPair.getPublicKey();
        this.privateKey = keyPair.getPrivateKey(); // !!!  to do : send my public key to all other hosts
        myAddress.setPublicKey(publicKey); // !!! to be put into host_map

    }


    static public void main(String args[]){

    }


    class RequestSocket extends Thread{

    }

}
