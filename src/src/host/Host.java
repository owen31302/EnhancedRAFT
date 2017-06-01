package host;

import Communicator.TCP_ReplyMsg_All;
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
    private String hostName;
    private StateManager stateManager;
    private Integer currentTerm;
    private int charactor;
    private Leader leader;
    private Follower follower;
    private Candidate candidate;
    private HostAddress votedFor;
    private int commitIndex;
    private int lastApplied;
    //need to store public keys of other hosts
    private ServerSocket aServer;
    private RSAPrivateKey privateKey;
    private RSAPublicKey publicKey;
    private HostManager hostManager;
    private HostAddress myAddress;   // !!! to store my name, my ip, my port, my public key

    public Host(String hostName) throws IOException {
        this.hostName = hostName;
        stateManager = new StateManager();
        currentTerm = 0;

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
                // send user request (State) to leader.
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        switch ((int)arg){
            case CharacterManagement.F2C:
                System.out.println("Change from follower to Candidate.");
                follower.leave();
                charactor = CharacterManagement.CANDIDATE;
                candidate = new Candidate(this);
                candidate.addObserver(this);
                Thread candidateThread = new Thread(candidate);
                candidateThread.start();
                break;
            case CharacterManagement.C2L:
                candidate.leave();
                charactor = CharacterManagement.LEADER;
                // leader should take care of non-up-to-date followers.
                leader = new Leader(this, candidate.get_tcp_ReplyMsg_All());
                leader.addObserver(this);
                Thread leaderThread = new Thread(leader);
                leaderThread.start();
                break;
            case CharacterManagement.C2F:
                // This only happens at the receiver side ...
                // When new leader is elected ...
                break;
            case CharacterManagement.L2F:
                // This only happens at the receiver side ...
                // new leader wins the term
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
    public RSAPrivateKey getPrivateKey(){
        return privateKey;
    }
    public RSAPublicKey getPublicKey(){
        return publicKey;
    }
    public HostManager getHostManager(){
        return hostManager;
    }
    public String getHostName(){
        return hostName;
    }
    public StateManager getStateManager(){
        return stateManager;
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