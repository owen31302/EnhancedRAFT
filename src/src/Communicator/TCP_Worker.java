package Communicator;

import host.HostAddress;
import signedMethods.SignedMessage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.interfaces.RSAPublicKey;

/**
 * Created by shan on 5/29/17.
 */
public class TCP_Worker extends Thread {
    private Socket clientSocket;
    private HostAddress target;
    private TCP_ReplyMsg_All tcp_ReplyMsg_All;
    private TCP_ReplyMsg_One tcp_ReplyMsg_One;
    private RSAPublicKey publicKey;
    private String jobType;
    private SignedMessage msg;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    /**
     * Constructor for initSendToAll() in Communicator
     * @param target specific host you want to build connection
     * @param tcp_ReplyMsg_All a container to store replied information
     * @param msg message you want to send
     * @param publicKey public key of target host
     * @param jobType current job type
     */
    public TCP_Worker(HostAddress target, TCP_ReplyMsg_All tcp_ReplyMsg_All, SignedMessage msg, RSAPublicKey publicKey, String jobType) {
        this.target = target;
        this.tcp_ReplyMsg_All = tcp_ReplyMsg_All;
        this.msg = msg;
        this.publicKey = publicKey;
        this.jobType = jobType;
    }

    /**
     * Constructor for initSendToOne() in Communicator
     * @param target specific host you want to build connection
     * @param tcp_replyMsg_One a container to store replied message
     * @param msg message you want to send
     * @param publicKey public key of target host
     * @param jobType current job type
     */
    public TCP_Worker(HostAddress target, TCP_ReplyMsg_One tcp_replyMsg_One, SignedMessage msg, RSAPublicKey publicKey, String jobType) {
        this.target = target;
        this.tcp_ReplyMsg_One = tcp_replyMsg_One;
        this.msg = msg;
        this.publicKey = publicKey;
        this.jobType = jobType;
    }

    /**
     * Constructor for receiveFromOne() in OnewayCommunicationPackage
     * @param clientSocket socket you received and want to communicate with
     * @param tcp_replyMsg_One a container to store received message
     * @param jobType current job type
     */
    public TCP_Worker(Socket clientSocket, TCP_ReplyMsg_One tcp_replyMsg_One, String jobType) {
        this.clientSocket = clientSocket;
        this.tcp_ReplyMsg_One = tcp_replyMsg_One;
        this.jobType = jobType;
    }


    /**
     * Start running this thread
     */
    public void run() {
        boolean DEBUG = false;
        if (DEBUG) System.out.println("From TCP_worker: enter run()");

        openConnection();

        try {
            handleRequest();
        } catch (IOException | ClassNotFoundException e) {
            if (DEBUG)
                System.out.println("Warning!!! IOexception caught @ handleRequest()");
            if (tcp_ReplyMsg_All != null)
                this.tcp_ReplyMsg_All.addFailedNode(this.target); // if connection fail, record the failed node
        }
    }

    /**
     * Open connection with target host, if no connection build yet. If already connected, use connected socket.
     */
    public void openConnection() {
        boolean DEBUG = false;
        if (DEBUG) System.out.println("From TCP_worker: enter openConnection()");

        try {
            if (this.jobType.equals(JobType.sentToOne) || this.jobType.equals(JobType.sentToAll)) {
                clientSocket = new Socket(target.getHostIp(), target.getHostPort());
                if (DEBUG) System.out.println("From TCP_worker: socket is created ");
            }
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(clientSocket.getInputStream());
        } catch (IOException e) {
            if (DEBUG)
                System.out.println("Warning!!! IOexception caught @ open connection");
            if (tcp_ReplyMsg_All != null)
                this.tcp_ReplyMsg_All.addFailedNode(this.target); // if connection fail, record the failed node
        }
        if (DEBUG) System.out.println("From TCP_worker: return from openConnection()");
    }

    /**
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void handleRequest() throws IOException, ClassNotFoundException {
        boolean DEBUG = false;
        if (DEBUG) System.out.println("From TCP_worker: enter openConnection()");

        // sent to one, round trip
        if (this.jobType.equals(JobType.sentToOne)) {
            if (DEBUG) System.out.println("From TCP_worker: jobType is sentToOne = " + this.jobType);
            out.writeObject(msg);
            if (DEBUG) System.out.println("From TCP_worker: msg is sent ");

            // out.flush();
            SignedMessage replyMsg = (SignedMessage) in.readObject();
            if (DEBUG) System.out.println("From TCP_worker: reply is received ");
            if (replyMsg.getMessageType().equals(this.msg.getMessageType())) {
                this.tcp_ReplyMsg_One.setMessage(replyMsg);
                if (DEBUG) System.out.println("From TCP_worker: reply message type match! ");

            }
            closeConnection();
            if (DEBUG) System.out.println("From TCP_worker: return from handleRequest()");
            return;
        }

        // sent to all, round trip
        if (this.jobType.equals(JobType.sentToAll)){
            if (DEBUG) System.out.println("From TCP_worker: jobType is sentToAll = " + this.jobType);
            out.writeObject(msg);
            if (DEBUG) System.out.println("From TCP_worker: msg is sent ");
            //  out.flush();
            SignedMessage replyMsg = (SignedMessage) in.readObject();
            if (DEBUG) System.out.println("From TCP_worker: reply is received ");
            if (replyMsg.getMessageType().equals(this.msg.getMessageType())) {
                if (DEBUG) System.out.println("From TCP_worker: reply message type match ");
                if (SignedMessage.decrypt(this.publicKey, replyMsg.getEncryptedMessageContent()).equals("Yes")) {
                    if (DEBUG) System.out.println("From TCP_worker: reply message is \"Yes\" ");
                    this.tcp_ReplyMsg_All.addRepliedNode(this.target);
                } else {
                    if (DEBUG) System.out.println("From TCP_worker: reply message is \"No\" or other");
                }
            }
            closeConnection();
            if (DEBUG) System.out.println("From TCP_worker: return from handleRequest()");
            return;
        }

        // receive from one specified client socket, one trip

        if (this.jobType.equals(JobType.receiveFromOne)) {
            if (DEBUG) System.out.println("From TCP_worker: jobType is receiveFromOne = " + this.jobType);

            SignedMessage replyMsg = (SignedMessage) in.readObject();
            if (DEBUG) System.out.println("From TCP_worker: message is received ");

            this.tcp_ReplyMsg_One.setMessage(replyMsg);

            synchronized (this.tcp_ReplyMsg_One) {
                this.tcp_ReplyMsg_One.notifyAll();
                if (DEBUG) System.out.println("From TCP_worker: notify that message is received ");

            }

            // wait for jobType to be changed to replyToOne, then send reply, one trip
            synchronized (this.jobType) {
                while (this.msg == null) {
                    if (DEBUG) System.out.println("From TCP_worker: no message to be sent yet, waiting...");
                    try {
                        this.jobType.wait();
                        if (DEBUG) System.out.println("From TCP_worker: got notify job type is changed ! ");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            // reply to one specified client socket, one trip
            if (this.jobType.equals(JobType.replyToOne)) {
                if (DEBUG) System.out.println("From TCP_worker: job type is changed to " + this.jobType);
                out.writeObject(msg);
                if (DEBUG) System.out.println("From TCP_worker: message is sent ");
                this.tcp_ReplyMsg_One.setMessage(new SignedMessage("", "sent", null)); // only indicate sent
            }

            synchronized(this.tcp_ReplyMsg_One) {
                this.tcp_ReplyMsg_One.notifyAll();
                if (DEBUG) System.out.println("From TCP_worker: notify message is sent ");
            }

            closeConnection();
            if (DEBUG) System.out.println("From TCP_worker: return from handleRequest()");
            return;
        }
    }

    /**
     *
     * @throws IOException
     */
    public void closeConnection() throws IOException {
        boolean DEBUG = false;
        if (DEBUG) System.out.println("From TCP_worker: enter closeConnection()");

        if (in != null) {
            in.close();
        }
        if (out != null) {
            out.close();
        }

        this.clientSocket.close();
        if (DEBUG) System.out.println("From TCP_worker: close successfully()");

    }

    /**
     *
     * @return
     */
    public String getJobType() {
        return jobType;
    }

    /**
     *
     * @param jobType
     */
    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    /**
     *
     * @return
     */
    public SignedMessage getMsg() {
        return msg;
    }

    /**
     *
     * @param msg
     */
    public void setMsg(SignedMessage msg) {
        this.msg = msg;
    }

    /**
     *
     * @return
     */
    public TCP_ReplyMsg_One getTcp_ReplyMsg_One() {
        return tcp_ReplyMsg_One;
    }

    /**
     *
     * @param tcp_ReplyMsg_One
     */
    public void setTcp_ReplyMsg_One(TCP_ReplyMsg_One tcp_ReplyMsg_One) {
        this.tcp_ReplyMsg_One = tcp_ReplyMsg_One;
    }
}
