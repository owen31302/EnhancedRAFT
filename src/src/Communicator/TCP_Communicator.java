package Communicator;

import host.HostManager;

/**
 * Created by shan on 5/29/17.
 */
public class TCP_Communicator {
    private HostManager hostManager;
    private TCP_AllReply tcp_allReply;

    public TCP_Communicator(HostManager hostManager, TCP_AllReply tcp_allReply) {
        this.hostManager = hostManager;
        this.tcp_allReply = tcp_allReply;
    }
}
