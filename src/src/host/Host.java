package host;

import Communicator.OnewayCommunicationPackage;
import Communicator.TCP_Communicator;
import signedMethods.Keys;
import signedMethods.SignedMessage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.HashMap;
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
    private Thread followerThread;
    private Integer votedTerm;

    public Host() throws IOException {
        hostName = InetAddress.getLocalHost().getHostAddress();
        stateManager = new StateManager(new String[]{"x", "y", "z"});
        currentTerm = 0;
        commitIndex = 0;
        votedTerm = 0;
        charactor = CharacterManagement.FOLLOWER;
        follower = new Follower(stateManager);
        follower.addObserver(this);

        aServer = new ServerSocket(0);
        System.out.println(InetAddress.getLocalHost().getHostAddress() + " at port number: " + aServer.getLocalPort());
        Keys keyPair = new Keys();
        this.publicKey = keyPair.getPublicKey();
        this.privateKey = keyPair.getPrivateKey(); // !!!  to do : send my public key to all other hosts
        myAddress = new HostAddress(hostName, InetAddress.getLocalHost().getHostAddress(), aServer.getLocalPort());
        myAddress.setPublicKey(publicKey); // !!! to be put into host_map
        hostManager = new HostManager(myAddress);
    }

    @Override
    public void run() {

        while (true){
            try {
                RequestResponse aRequest = new RequestResponse(aServer.accept(), -1, null);
                aRequest.start();
                System.out.println("get requst");
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
                currentTerm++;
                follower.leave();
                charactor = CharacterManagement.CANDIDATE;
                candidate = new Candidate(this);
                candidate.addObserver(this);
                Thread candidateThread = new Thread(candidate);
                candidateThread.setDaemon(true);
                candidateThread.start();
                break;
            case CharacterManagement.C2L:
                candidate.leave();
                charactor = CharacterManagement.LEADER;
                // leader should take care of non-up-to-date followers.
                leader = new Leader(this, candidate.get_tcp_ReplyMsg_All());
                leader.addObserver(this);
                Thread leaderThread = new Thread(leader);
                leaderThread.setDaemon(true);
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
    public int getCommitIndex(){
        return commitIndex;
    }
    public int getLastApplied(){
        return lastApplied;
    }
    public void setCommitIndex(int index){
        commitIndex = index;
    }

    class RequestResponse extends Thread {
        private Socket aSocket;
        private ObjectOutputStream oOut;
        private ObjectInputStream oIn;
        private int command;
        private Object parameter; // this is

        RequestResponse(Socket aSocket, int command, Object parameter) {
            this.aSocket = aSocket;
            this.parameter = parameter;
            this.command = command; // if command is -1, receive message first

        }

        RequestResponse(HostAddress hostAddress, int command, Object parameter) throws IOException {
            this.command = command;
            this.parameter = parameter;
            aSocket = new Socket(hostAddress.getHostIp(), hostAddress.getHostPort());
        }

        @Override
        public void run() {
            try {
                System.out.println("IP:" + aSocket.getInetAddress().getHostAddress());
                if (hostManager.isInHostList(aSocket.getInetAddress().getHostAddress())) {
                    command = Protocol.RPCREQUEST;
                }
                else{
                    oOut = new ObjectOutputStream(aSocket.getOutputStream());
                    oOut.flush();
                    oIn = new ObjectInputStream(aSocket.getInputStream());
                    if (command == -1) {
                        command = oIn.readInt();
                    }
                }

                System.out.println("command:" + command);
                switch (command) {
                    case Protocol.AddHostAddresses:
                        ArrayList<HostAddress> hostAddressArrayList = (ArrayList<HostAddress>)(oIn.readObject());
                        //asking other host for their public key
                        RequestResponse[] otherHosts = new RequestResponse[hostAddressArrayList.size() - 1];
                        ArrayList<HostAddress> unavailableHost = new ArrayList<HostAddress>();
                        int i = 0;
                        for (HostAddress a: hostAddressArrayList){ //ask other hosts' public key
                            if (!a.equals(myAddress)) {
                                try {
                                    otherHosts[i] = new RequestResponse(a, Protocol.ASKHOSTNAME, a);
                                    otherHosts[i].start();
                                    i++;
                                } catch (IOException e){
                                    hostAddressArrayList.remove(a);
                                    unavailableHost.add(a);
                                }
                            }
                            else {
                                //hostAddressArrayList.remove(a);
                            }
                        }

                        for (int j = 0; j < i; j++) { //waiting for getting public keys
                            try {
                                otherHosts[j].join();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

//                        for (int j = 0; j < i; j++) {
//                            otherHosts[j] = new RequestResponse(hostAddressArrayList.get(j), Protocol.UPDATEHOSTLIST, null);
//                            otherHosts[j].start();
//
//                        }

                        i = 0;
                        for (HostAddress a: hostAddressArrayList){ //ask other hosts' public key
                            if (!a.equals(myAddress)) {
                                try {
                                    otherHosts[i] = new RequestResponse(a, Protocol.UPDATEHOSTLIST, null);
                                    otherHosts[i].start();
                                    i++;
                                } catch (IOException e){
                                    hostAddressArrayList.remove(a);
                                    unavailableHost.add(a);
                                }
                            }
                            else {
                                //hostAddressArrayList.remove(a);
                            }
                        }

                        for (int j = 0; j < i; j++) {
                            try {
                                otherHosts[j].join();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        oOut.writeInt(Protocol.Ackowledgement);
                        oOut.flush();
                        System.out.println(hostManager);
                        followerThread = new Thread( follower );
                        followerThread.setDaemon(true);
                        followerThread.start();
                        break;

                    case Protocol.ASKHOSTNAME:
                        oOut.writeInt(Protocol.REPLYHOSTNAME);
                        oOut.flush();
                        System.out.println("send");
                        HostAddress temp = (HostAddress)(parameter);
                        temp.setHostName((String)oIn.readObject());
                        temp.setPublicKey((RSAPublicKey)oIn.readObject());
                        oOut.writeInt(Protocol.Ackowledgement);
                        oOut.flush();
                        hostManager.addHostToList(temp);
                        break;

                    case Protocol.REPLYHOSTNAME:
                        oOut.writeObject(hostName);
                        oOut.writeObject(publicKey);
                        if (oIn.readInt() != Protocol.Ackowledgement){
                            System.out.println("Error happened when sending host name");
                        }
                        break;

                    case Protocol.UPDATEHOSTLIST:
                        oOut.writeInt(Protocol.REPLYHOSTLIST);
                        oOut.flush();
                        oOut.writeObject(hostManager.getHostList());
                        if(oIn.readInt() != Protocol.Ackowledgement){
                            System.out.println("Error happened when updating host list");
                        }
                        break;

                    case Protocol.REPLYHOSTLIST:
                        hostManager.replaceHostList((HashMap<String ,HostAddress>)oIn.readObject());
                        oOut.writeInt(Protocol.Ackowledgement);
                        oOut.flush();
                        System.out.println(hostManager);
                        followerThread = new Thread( follower );
                        followerThread.setDaemon(true);
                        followerThread.start();
                        break;

                    case Protocol.RPCREQUEST:
                        TCP_Communicator tempTCP = new TCP_Communicator(privateKey);
                        OnewayCommunicationPackage onewayCommunicationPackage = new OnewayCommunicationPackage(aSocket);
                        SignedMessage receivedMSG = tempTCP.receiveFromOne(onewayCommunicationPackage);
                        String RPC = receivedMSG.getMessageType();
                        HostAddress requestHost = hostManager.getHostAddress(aSocket.getInetAddress().getHostName());
                        String planText = receivedMSG.getPlanText(hostManager.getPublicKey(aSocket.getInetAddress().getHostAddress()));
                        System.out.println("Plan text: " + planText);
                        switch (RPC) {
                            case RPCs.REQUESTVOTE:
                                String[] aurgments = planText.split(",");
                                int candidateTerm = Integer.parseInt(aurgments[0]);
                                int lastLogIndex = Integer.parseInt(aurgments[2]);
                                int lastLogTerm = Integer.parseInt(aurgments[3]);
                                synchronized (votedTerm) {
                                    if (votedTerm < candidateTerm && currentTerm <= candidateTerm
                                            && stateManager.getLastIndex() <= lastLogIndex && stateManager.getLastLog().getTerm() <= lastLogTerm) {
                                        votedTerm++;
                                        SignedMessage signedMessage = new SignedMessage(RPCs.REQUESTVOTE, "grant", requestHost.getPublicKey());
                                        tempTCP.replyToOne(onewayCommunicationPackage, signedMessage);
                                        System.out.println("grant vote");
                                    }
                                    break;
                                }
                        }
                        break;
                }

                if (oOut != null) {
                    oOut.close();
                }

                if (oIn != null) {
                    oIn.close();
                }

                if (!aSocket.isClosed()) {
                    aSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

        }
    }

    static public void main(String args[]) {
//        try {
//            Host test = new Host("test1");
//            Thread.sleep(100); // simulate heartbeat
//            test.follower.receivedHeartBeat();
//            Thread.sleep(100); // simulate appendEntries
//            host.State state = new host.State("x", 1);
//            test.follower.appendAnEntry(state, test.currentTerm);
//            Thread.sleep(1000);
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (InterruptedException e2){
//            e2.printStackTrace();
//        }
        try {
            Host aHost = new Host();
            aHost.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}