package Communicator;

import host.HostAddress;
import host.HostManager;
import signedMethods.SignedMessage;

import java.security.interfaces.RSAPrivateKey;
import java.util.Map;

/**
 * Created by shan on 5/29/17.
 */
public class TCP_Communicator {
    private RSAPrivateKey privateKey;

    public TCP_Communicator(RSAPrivateKey privateKey) {
        this.privateKey = privateKey;
    }
    /**
     * Broadcast message to all hosts, count if replies reach majority
     * @param hostManager give host manager who has all hosts infor
     * @param tcp_ReplyMsg_All reply nodes' info will be put into this object
     * @param msg message to be sent to all hosts
     * @return if replies reach majority
     */
    public boolean sendToAll(HostManager hostManager, TCP_ReplyMsg_All tcp_ReplyMsg_All, SignedMessage msg) {

        for (Map.Entry<String, HostAddress> a : hostManager.getHostList().entrySet()) {
            HostAddress targetHost = a.getValue();
            TCP_Worker worker = new TCP_Worker(targetHost, tcp_ReplyMsg_All, msg, this.privateKey);
            worker.start();
        }
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return tcp_ReplyMsg_All.getList_repliedHost().size() >= hostManager.getHostList().size() / 2 + 1;
    }

    /**
     * Send message to a specific host. No check of message content in this method.
     * @param targetHost host address
     * @param tcp_ReplyMsg_One reply message will be put into this object
     * @param msg message to be sent to target host
     * @return true if there is message; false if no reply or target node fails
     */
    public boolean sendToOne(HostAddress targetHost, TCP_ReplyMsg_One tcp_ReplyMsg_One, SignedMessage msg) {
        TCP_Worker worker = new TCP_Worker(targetHost, tcp_ReplyMsg_One, msg, this.privateKey);
        worker.start();
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return tcp_ReplyMsg_One.getMessage() != null;
    }

}