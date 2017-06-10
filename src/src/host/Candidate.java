package host;

import Communicator.TCP_Communicator;
import Communicator.TCP_ReplyMsg_All;
import signedMethods.SignedMessage;
import java.util.Observable;

/**
 * Created by owen on 5/26/17.
 */
public class Candidate extends Observable implements Runnable {
    private boolean _closed = false;
    private Host _host;
    private TCP_ReplyMsg_All _tcp_ReplyMsg_All;

    public Candidate(Host host){
        _host = host;
    }

    @Override
    public void run() {
        System.out.println("I am Candidate!");
        boolean result = false;
        SignedMessage signedMessage;
        TCP_Communicator tcp_communicator = new TCP_Communicator();

        while (!result && !_closed){
            _tcp_ReplyMsg_All = new TCP_ReplyMsg_All();
            // requestVoteArray[0] = term
            // requestVoteArray[1] = candidateId
            // requestVoteArray[2] = lastLogIndex
            // requestVoteArray[3] = lastLogTerm
            String[] requestVoteArray = new String[] {
                    _host.getCurrentTerm().toString(),
                    _host.getHostName(),
                    String.valueOf(_host.getStateManager().getLastLog().getIndex()),
                    String.valueOf(_host.getStateManager().getLastLog().getTerm())
            };
            String requestVote = String.join(",", requestVoteArray);
            signedMessage = new SignedMessage(RPCs.REQUESTVOTE, requestVote, _host.getPrivateKey());
            result = tcp_communicator.initSendToAll(_host.getHostManager(), _tcp_ReplyMsg_All, signedMessage);
            System.out.println(result);
        }
        if(result){
            System.out.println("Changed!");
            setChanged();
            notifyObservers(CharacterManagement.C2L);
        }
    }

    public void leave(){
        _closed = true;
    }
    public TCP_ReplyMsg_All get_tcp_ReplyMsg_All(){
        return _tcp_ReplyMsg_All;
    }
}
