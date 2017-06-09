package Communicator;

import host.HostAddress;
import host.HostManager;
import signedMethods.SignedMessage;

import java.util.Map;

/**
 * Created by shan on 5/29/17.
 */
public class TCP_Communicator {
    public TCP_Communicator() {}

    /**
     * Broadcast message to all hosts, count if replies reach majority
     * @param hostManager give host manager who has all hosts infor
     * @param tcp_ReplyMsg_All reply nodes' info will be put into this object
     * @param msg message to be sent to all hosts
     * @return if replies reach majority
     */
    public boolean initSendToAll(HostManager hostManager, TCP_ReplyMsg_All tcp_ReplyMsg_All, SignedMessage msg) {
        boolean DEBUG = true;
        if (DEBUG) System.out.println("From communicator: enter initSendToAll()");
        for (Map.Entry<String, HostAddress> a : hostManager.getHostList().entrySet()) {
            HostAddress targetHost = a.getValue();
            TCP_Worker worker = new TCP_Worker(targetHost, tcp_ReplyMsg_All, msg, targetHost.getPublicKey(), JobType.sentToAll);
            worker.start();
            if (DEBUG) System.out.println("From communicator: worker started with " + targetHost.getHostName() + ", " + targetHost.getHostIp());
        }
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (DEBUG) System.out.println("From communicator: 200 ms timeout, return from initSendToOne()");
        return tcp_ReplyMsg_All.getList_repliedHost().size() >= hostManager.getHostList().size() / 2 + 1;
    }

    /**
     * Send message to a specific host. No check of message content in this method.
     * @param targetHost host address
     * @param tcp_ReplyMsg_One reply message will be put into this object
     * @param msg message to be sent to target host
     * @return true if there is message; false if no reply or target node fails
     */
    public boolean initSendToOne(HostAddress targetHost, TCP_ReplyMsg_One tcp_ReplyMsg_One, SignedMessage msg) {
        boolean DEBUG = true;
        if (DEBUG) System.out.println("From communicator: enter initSendToOne()");

        TCP_Worker worker = new TCP_Worker(targetHost, tcp_ReplyMsg_One, msg, targetHost.getPublicKey(), JobType.sentToOne);
        worker.start();
        if (DEBUG) System.out.println("From communicator: worker started with " + targetHost.getHostName() + ", " + targetHost.getHostIp());

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (DEBUG) System.out.println("From communicator: 200 ms timeout, return from initSendToOne()");
        return tcp_ReplyMsg_One.getMessage() != null;
    }

    /**
     * Got a connected socket, use this method to get the message it sends.
     * @param pkg a wrapper object to maintain one round-trip communication
     * @return received message
     */
    public SignedMessage receiveFromOne(OnewayCommunicationPackage pkg) {
        return pkg.receiveFromOne();
    }

    /**
     * Send reply to whoever sends you a message before.
     * @param pkg a wrapper object to maintain one round-trip communication, should be the same with the one used in receiveFromOne()
     * @param msg message you want to send
     * @return true if message send successfully
     */
    public boolean replyToOne(OnewayCommunicationPackage pkg, SignedMessage msg) {
        return pkg.replyToOne(msg);
    }




}