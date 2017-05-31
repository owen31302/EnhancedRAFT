package host;

import signedMethods.Keys;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by TC_Yeh on 5/26/2017.
 */
public class Host extends Thread implements Observer{
    private StateManager stateManager;
    private Integer term;
    //need to store public keys of other hosts
    private ServerSocket aServer;
    private Leader leader;
    private Follower follower;
    private Candidate candidate;
    private RSAPrivateKey privateKey;
    private RSAPublicKey publicKey;
    private HostManager hostManager;
    private HostAddress myAddress;   // !!! to store my name, my ip, my port, my public key
    private int charactor;

    public Host(String hostName) throws IOException {
        stateManager = new StateManager();
        leader = new Leader();

        follower = new Follower(stateManager);
        follower.addObserver(this);
        Thread followerThread = new Thread( follower );
        followerThread.start();
        charactor = CharacterManagement.FOLLOWER;

        candidate = new Candidate();
        term = 0;
        aServer = new ServerSocket(0);
        System.out.println(aServer.getInetAddress().getHostAddress() + " at port number: " + aServer.getLocalPort());
        Keys keyPair = new Keys();
        this.publicKey = keyPair.getPublicKey();
        this.privateKey = keyPair.getPrivateKey(); // !!!  to do : send my public key to all other hosts
        myAddress = new HostAddress(hostName, aServer.getInetAddress().getHostAddress(), aServer.getLocalPort());
        myAddress.setPublicKey(publicKey); // !!! to be put into host_map
        hostManager = new HostManager(myAddress);
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

    @Override
    public void update(Observable o, Object arg) {
        if((int)arg == CharacterManagement.F2C){
            System.out.println("I want to become candidate.");
        }else{
            System.out.println("Something wrong.");
        }
    }

    static public void main(String args[]) {
        try {
            Host test = new Host("test1");
            Thread.sleep(100); // simulate heartbeat
            test.follower.receivedHeartBeat();
            Thread.sleep(100); // simulate appendEntries
            host.State state = new host.State("x", 1);
            test.follower.appendAnEntry(state, test.term);
            Thread.sleep(1000);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e2){
            e2.printStackTrace();
        }
    }
}