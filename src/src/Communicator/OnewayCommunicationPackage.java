package Communicator;

import signedMethods.SignedMessage;

import java.net.Socket;

/**
 * Created by shan on 6/9/17.
 */
public class OnewayCommunicationPackage {
    private TCP_Worker tcp_worker;
    private Socket clientSocket;
    private TCP_ReplyMsg_One reply_success;
    private TCP_ReplyMsg_One received_Msg;
    private SignedMessage reply_msg;
    private String jobType;
    private boolean global_DEBUG = false;

    /**
     * Constructor
     * @param clientSocket socket that has connected
     */
    public OnewayCommunicationPackage(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    /**
     * Receive from one connected socket
     * @return received message from this socket
     */
    public SignedMessage receiveFromOne() {
        boolean local_DEBUG = true;
        boolean DEBUG = global_DEBUG? local_DEBUG: global_DEBUG;
        if (DEBUG) System.out.println("From onewayCommu: enter receiveFromOne()");

        this.received_Msg = new TCP_ReplyMsg_One();
        this.tcp_worker = new TCP_Worker(this.clientSocket, this.received_Msg, JobType.receiveFromOne);
        this.tcp_worker.start();
        if (DEBUG) System.out.println("From onewayCommu: worker started with " + this.clientSocket.getInetAddress());

        // wait for receive something
        synchronized (this.received_Msg) {
            while (this.received_Msg.getMessage() == null) {
                if (DEBUG) System.out.println("From onewayCommu: no received msg yet, waiting...");
                try {
                    this.received_Msg.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (DEBUG) System.out.println("From onewayCommu: got notified, check if there is msg");
            }
        }
        if (DEBUG) System.out.println("From onewayCommu: there is msg, return from receiveFromOne()");

        return this.received_Msg.getMessage();
    }

    /**
     * Reply to one connected socket
     * @param reply_msg message you want to send
     * @return if message is sent succefully
     */
    public boolean replyToOne(SignedMessage reply_msg) {
        boolean local_DEBUG = true;
        boolean DEBUG = global_DEBUG? local_DEBUG: global_DEBUG;
        if (DEBUG) System.out.println("From onewayCommu: enter replyToOne()");

        this.tcp_worker.getTcp_ReplyMsg_One().setMessage(null); // erase it for get new reply
        this.reply_success = tcp_worker.getTcp_ReplyMsg_One();
        this.tcp_worker.setMsg(reply_msg);

        this.tcp_worker.setJobType(JobType.replyToOne);
        this.jobType = this.tcp_worker.getJobType();

        synchronized (this.jobType) {
            this.jobType.notifyAll();
        }
        if (DEBUG) System.out.println("From onewayCommu: notify that job type is changed!");

        synchronized(this.reply_success) {
            while (this.reply_success.getMessage() == null) {
                if (DEBUG) System.out.println("From onewayCommu: not sent yet, waiting...");
                try {
                    this.reply_success.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        if (DEBUG) System.out.println("From onewayCommu: sent successfully, return from receiveFromOne()");
        return this.reply_success.getMessage() != null; // if not null = sent successful
    }

    /**
     * Get tcp_worker
     * @return
     */
    public TCP_Worker getTcp_worker() {
        return tcp_worker;
    }

    /**
     * Set tcp_worker
     * @param tcp_worker
     */
    public void setTcp_worker(TCP_Worker tcp_worker) {
        this.tcp_worker = tcp_worker;
    }

    /**
     * Get reply_msg
     * @return
     */
    public SignedMessage getReply_msg() {
        return reply_msg;
    }

    /**
     * Set reply_msg
     * @param reply_msg
     */
    public void setReply_msg(SignedMessage reply_msg) {
        this.reply_msg = reply_msg;
    }

}
