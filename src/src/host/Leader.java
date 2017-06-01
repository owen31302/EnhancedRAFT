package host;

import Communicator.TCP_ReplyMsg_All;

import java.util.Observable;

/**
 * Created by TC_Yeh on 5/26/2017.
 */
public class Leader extends Observable implements Runnable {
    private boolean _closed = false;
    private int _nextIndex[];
    private int _matchIndex[];
    private Host _host;
    private TCP_ReplyMsg_All _tcp_replyMsg_all;

    public Leader(Host host, TCP_ReplyMsg_All tcp_replyMsg_all){
        _host = host;
        _tcp_replyMsg_all = tcp_replyMsg_all;
        int numberOfHosts = _host.getHostManager().getHostList().size();
        _nextIndex = new int[numberOfHosts-1];
        _matchIndex = new int[numberOfHosts-1];
    }

    @Override
    public void run() {

    }
}
