package Communicator;

import host.HostAddress;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shan on 5/29/17.
 */
public class TCP_ReplyMsg_All {
    private List<HostAddress> list_unConnectableHost;
    private List<HostAddress> list_repliedHost;

    public TCP_ReplyMsg_All() {
        list_unConnectableHost = new ArrayList<>();
        list_repliedHost = new ArrayList<>();
    }
    public List<HostAddress> getList_unConnectableHost() {
        return list_unConnectableHost;
    }

    public List<HostAddress> getList_repliedHost() {
        return list_repliedHost;
    }

    public void addFailedNode(HostAddress host) {
        this.list_unConnectableHost.add(host);
    }

    public void addRepliedNode(HostAddress host) {
        this.list_repliedHost.add(host);
    }
}
