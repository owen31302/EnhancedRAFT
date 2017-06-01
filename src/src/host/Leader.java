package host;

import Communicator.TCP_Communicator;
import Communicator.TCP_ReplyMsg_All;
import signedMethods.SignedMessage;

import java.util.LinkedList;
import java.util.Observable;
import java.util.Queue;

/**
 * Created by TC_Yeh on 5/26/2017.
 */
public class Leader extends Observable implements Runnable {
    private boolean _closed = false;
    private int _nextIndex[];
    private boolean _findNextIndex[];
    private Host _host;
    private TCP_ReplyMsg_All _tcp_replyMsg_all;
    private Queue<State> _userRequestQueue;
    private int _numberOfHosts;

    public Leader(Host host, TCP_ReplyMsg_All tcp_replyMsg_all){
        _host = host;
        _tcp_replyMsg_all = tcp_replyMsg_all;
        _numberOfHosts = _host.getHostManager().getHostList().size();
        _nextIndex = new int[_numberOfHosts-1];
        _findNextIndex = new boolean[_numberOfHosts-1];
        int lastIndex = _host.getStateManager().getLastIndex();
        for(int i  = 0; i<_numberOfHosts; i++){
            _nextIndex[i] = lastIndex;
        }
        _userRequestQueue = new LinkedList<>();
    }

    @Override
    public void run() {
        // (1)  get the latest log index for each host
        //      update the log for non-up-to-date hosts
        //      1. initial each log index to the latest index (done in constructor)
        //      2. find the matching index and append the remaining logs
        // (2)  append the user request
        // (3)  send the heartbeat
        // (1), (2), (3) will happen concurrently.

        SignedMessage signedMessage;
        TCP_Communicator tcp_communicator = new TCP_Communicator(_host.getPrivateKey());
        while (!_closed){
            for(int i = 0 ; i < _numberOfHosts ; i++ ){
                if(!_findNextIndex[i]){

                }else if(_nextIndex[i] != _host.getCommitIndex()){

                }else if(!_userRequestQueue.isEmpty()){

                }
            }
        }
    }
}
