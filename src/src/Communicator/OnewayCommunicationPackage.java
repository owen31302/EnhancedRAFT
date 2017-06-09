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

    public OnewayCommunicationPackage(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public SignedMessage receiveFromOne() {
        this.received_Msg = new TCP_ReplyMsg_One();

        this.tcp_worker = new TCP_Worker(clientSocket, this.received_Msg, null, null, JobType.receiveFromOne);
        this.tcp_worker.start();

        // wait for receive something
        while (this.received_Msg.getMessage() == null) {
            try {
                this.received_Msg.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return this.received_Msg.getMessage();
    }

    public boolean replyToOne(SignedMessage reply_msg) {
        this.tcp_worker.getTcp_ReplyMsg_One().setMessage(null); // erase it for get new reply
        this.reply_success = tcp_worker.getTcp_ReplyMsg_One();
        this.tcp_worker.setMsg(reply_msg);

        this.tcp_worker.setJobType(JobType.replyToOne);
        this.jobType = this.tcp_worker.getJobType();

        synchronized (this.jobType) {
            this.jobType.notifyAll();
        }

        synchronized(this.reply_success) {
            while (this.reply_success.getMessage() == null) {
                try {
                    this.reply_success.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return this.reply_success.getMessage() != null; // if not null = sent successful
    }




    public TCP_Worker getTcp_worker() {
        return tcp_worker;
    }

    public void setTcp_worker(TCP_Worker tcp_worker) {
        this.tcp_worker = tcp_worker;
    }

    public SignedMessage getReply_msg() {
        return reply_msg;
    }

    public void setReply_msg(SignedMessage reply_msg) {
        this.reply_msg = reply_msg;
    }

}
