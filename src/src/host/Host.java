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
    private Integer currentTerm;
    private int charactor;
    private Leader leader;
    private Follower follower;
    private Candidate candidate;
    private HostAddress votedFor;
    private int commitIndex;
    private int lastApplied;
    private int nextIndex[];
    private int matchIndex[];
    private int numberOfHosts = 5;
    //need to store public keys of other hosts
    private ServerSocket aServer;
    private RSAPrivateKey privateKey;
    private RSAPublicKey publicKey;
    private HostManager hostManager;
    private HostAddress myAddress;   // !!! to store my name, my ip, my port, my public key

    public Host(String hostName) throws IOException {

        stateManager = new StateManager();
        currentTerm = 0;
        nextIndex = new int[numberOfHosts-1];
        matchIndex = new int[numberOfHosts-1];

        charactor = CharacterManagement.FOLLOWER;
        follower = new Follower(stateManager);
        follower.addObserver(this);
        Thread followerThread = new Thread( follower );
        followerThread.start();

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
        switch ((int)arg){
            case CharacterManagement.F2C:
                System.out.println("I want to become candidate.");
                follower.leave();
                break;
            case CharacterManagement.C2L:
                break;
            case CharacterManagement.C2F:
                break;
            case CharacterManagement.L2F: // new leader wins the term
                break;
            default:
                System.out.println("Something wrong.");
                break;
        }
    }

    public Integer getCurrentTerm(){
        return currentTerm;
    }
    public void setCurrentTerm(int term){
        currentTerm = term;
    }

    static public void main(String args[]) {
        try {
            Host test = new Host("test1");
            Thread.sleep(100); // simulate heartbeat
            test.follower.receivedHeartBeat();
            Thread.sleep(100); // simulate appendEntries
            host.State state = new host.State("x", 1);
            test.follower.appendAnEntry(state, test.currentTerm);
            Thread.sleep(1000);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e2){
            e2.printStackTrace();
        }
    }
}