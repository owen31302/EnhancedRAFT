package host;

import Communicator.TCP_Communicator;
import signedMethods.Keys;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * Created by TC_Yeh on 5/26/2017.
 */
public class Host extends Thread {
    private StateManager stateManager;
    //need to store public keys of other hosts
    private ServerSocket aServer;
    private Leader leader;
    private Follower follower;
    private Candidate candidate;
    private RSAPrivateKey privateKey;
    private RSAPublicKey publicKey;
    private HostManager hostManager;
    private HostAddress myAddress;   // !!! to store my name, my ip, my port, my public key
    private int term;
    private TCP_Communicator communicator;

    public Host(String hostName) throws IOException {
        stateManager = new StateManager();
        leader = new Leader();
        follower = new Follower(stateManager);
        candidate = new Candidate();
        term = 0;
        aServer = new ServerSocket(0);
        System.out.println(aServer.getInetAddress().getHostAddress() + " at port number: " + aServer.getLocalPort());
        Keys keyPair = new Keys();
        this.publicKey = keyPair.getPublicKey(); // !!!  to do : send my public key to all other hosts
        this.privateKey = keyPair.getPrivateKey();
        myAddress = new HostAddress(hostName, aServer.getInetAddress().getHostAddress(), aServer.getLocalPort());
        myAddress.setPublicKey(publicKey); // !!! to be put into host_map
        hostManager = new HostManager(myAddress);
        this.communicator = new TCP_Communicator(this.privateKey);
    }

    @Override
    public void run() {

        while (true){
            try {
                Socket aRequest= aServer.accept();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static public void main(String args[]) {
        try {
            Host test = new Host("test1");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}