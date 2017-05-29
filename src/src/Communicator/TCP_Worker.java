package Communicator;

import host.HostAddress;
import signedMethods.SignedMessage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.interfaces.RSAPrivateKey;

/**
 * Created by shan on 5/29/17.
 */
public class TCP_Worker extends Thread {
    private Socket clientSocket;
    private HostAddress target;
    private TCP_ReplyMsg_All tcp_ReplyMsg_All;
    private TCP_ReplyMsg_One tcp_ReplyMsg_One;
    private RSAPrivateKey privateKey;

    private SignedMessage msg;
    private ObjectInputStream in;
    private ObjectOutputStream out;


    public TCP_Worker(HostAddress target, TCP_ReplyMsg_All tcp_ReplyMsg_All, SignedMessage msg, RSAPrivateKey privateKey) {
        this.target = target;
        this.tcp_ReplyMsg_All = tcp_ReplyMsg_All;
        this.msg = msg;
        this.privateKey = privateKey;
    }

    public TCP_Worker(HostAddress target, TCP_ReplyMsg_One tcp_replyMsg_One, SignedMessage msg, RSAPrivateKey privateKey) {
        this.target = target;
        this.tcp_ReplyMsg_One = tcp_replyMsg_One;
        this.msg = msg;
        this.privateKey = privateKey;
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
            clientSocket = new Socket(target.getHostIp(), target.getHostPort());
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            in = new ObjectInputStream(clientSocket.getInputStream());
        } catch (IOException e) {
            if (DEBUG)
                System.out.println("IOexception caught");
            if (tcp_ReplyMsg_All != null)
                this.tcp_ReplyMsg_All.addFailedNode(this.target); // if connection fail, record the failed node
        }
    }

    public void handleRequest() throws IOException, ClassNotFoundException {
        out.writeObject(msg);
        SignedMessage replyMsg = (SignedMessage) in.readObject();

        if (this.tcp_ReplyMsg_One != null) {
            if (replyMsg.getMessageType().equals(this.msg.getMessageType())) {
                this.tcp_ReplyMsg_One.setMessage(replyMsg);
            }
        } else {
            if (replyMsg.getMessageType().equals(this.msg.getMessageType())) {
                if (SignedMessage.decrypt(this.privateKey, replyMsg.getEncryptedMessageContent()).equals("Yes")) {
                    this.tcp_ReplyMsg_All.addRepliedNode(this.target);
                }
            }
        }
    }
}
