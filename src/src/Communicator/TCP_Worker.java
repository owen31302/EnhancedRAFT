package Communicator;

import host.HostAddress;
import host.Protocol;
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

    public TCP_Worker(HostAddress target, TCP_ReplyMsg_All tcp_ReplyMsg_All, SignedMessage msg, RSAPublicKey publicKey, String jobType) {
        this.target = target;
        this.tcp_ReplyMsg_All = tcp_ReplyMsg_All;
        this.msg = msg;
        this.publicKey = publicKey;
        this.jobType = jobType;
    }

    public TCP_Worker(HostAddress target, TCP_ReplyMsg_One tcp_replyMsg_One, SignedMessage msg, RSAPublicKey publicKey, String jobType) {
        this.target = target;
        this.tcp_ReplyMsg_One = tcp_replyMsg_One;
        this.msg = msg;
        this.publicKey = publicKey;
        this.jobType = jobType;
    }

    public TCP_Worker(Socket clientSocket, TCP_ReplyMsg_One tcp_replyMsg_One, SignedMessage msg, RSAPublicKey publicKey, String jobType) {
        this.clientSocket = clientSocket;
        this.tcp_ReplyMsg_One = tcp_replyMsg_One;
        this.msg = msg;
        this.publicKey = publicKey;
        this.jobType = jobType;
    }


    public void run() {
        boolean DEBUG = false;
        openConnection();

        try {
            handleRequest();
        } catch (IOException | ClassNotFoundException e) {
            if (DEBUG)
                System.out.println("IOexception caught");
            if (tcp_ReplyMsg_All != null)
                this.tcp_ReplyMsg_All.addFailedNode(this.target); // if connection fail, record the failed node
        }
    }

    public void openConnection() {
        boolean DEBUG = false;
        try {
            if (this.jobType.equals(JobType.sentToOne) || this.jobType.equals(JobType.sentToAll)) {
                clientSocket = new Socket(target.getHostIp(), target.getHostPort());
            }
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(clientSocket.getInputStream());
        } catch (IOException e) {
            if (DEBUG)
                System.out.println("IOexception caught");
            if (tcp_ReplyMsg_All != null)
                this.tcp_ReplyMsg_All.addFailedNode(this.target); // if connection fail, record the failed node
        }
    }


    public void handleRequest() throws IOException, ClassNotFoundException {

        // sent to one, round trip
        if (this.jobType.equals(JobType.sentToOne)) {
            out.writeObject(msg);
           // out.flush();
            SignedMessage replyMsg = (SignedMessage) in.readObject();
            if (replyMsg.getMessageType().equals(this.msg.getMessageType())) {
                this.tcp_ReplyMsg_One.setMessage(replyMsg);
            }
            closeConnection();
        }

        // sent to all, round trip
        if (this.jobType.equals(JobType.sentToAll)){
            out.writeObject(msg);
          //  out.flush();
            SignedMessage replyMsg = (SignedMessage) in.readObject();
            if (replyMsg.getMessageType().equals(this.msg.getMessageType())) {
                if (SignedMessage.decrypt(this.publicKey, replyMsg.getEncryptedMessageContent()).equals("Yes")) {
                    this.tcp_ReplyMsg_All.addRepliedNode(this.target);
                }
            }
            closeConnection();
        }

        // receive from one specified client socket, one trip
        if (this.jobType.equals(JobType.receiveFromOne)) {
            SignedMessage replyMsg = (SignedMessage) in.readObject();
            this.tcp_ReplyMsg_One.setMessage(replyMsg);
            // Note: No need to close connection now, because you need to send back later.
            // After you send back, connection will be closed
        }

        // reply to one specified client socket, one trip
        if (this.jobType.equals(JobType.replyToOne)) {
            out.writeObject(msg);
            this.tcp_ReplyMsg_One.setMessage(new SignedMessage("", "sent", this.publicKey)); // only indicate sent
            closeConnection();
        }
    }

    public void closeConnection() throws IOException {
        if (in != null) {
            in.close();
        }
        if (out != null) {
            out.close();
        }

        this.clientSocket.close();
    }
}
