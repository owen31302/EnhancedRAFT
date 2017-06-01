package host;

import Communicator.TCP_Communicator;
import Communicator.TCP_ReplyMsg_One;
import signedMethods.SignedMessage;

/**
 * Created by Yu-Cheng Lin on 5/31/17.
 */
public class Leader_Worker implements Runnable {

    private String _hostName;
    private int _leaderJob;
    private Leader _leader;

    public Leader_Worker(Leader leader, int leaderJob, String hostName){
        _leader = leader;
        _leaderJob = leaderJob;
        _hostName = hostName;
    }

    @Override
    public void run() {
        SignedMessage signedMessage;
        TCP_Communicator tcp_communicator = new TCP_Communicator(_leader.get_host().getPrivateKey());
        TCP_ReplyMsg_One tcp_replyMsg_one = new TCP_ReplyMsg_One();
        switch (_leaderJob){
            case LeaderJobs.FINDINDEX:
                int index = _leader.get_nextIndex().get(_hostName);
                LogEntry logEntry = _leader.get_host().getStateManager().getLog(index);
                signedMessage = new SignedMessage(RPCs.APPENDENTRY, logEntry.getString(), _leader.get_host().getPrivateKey());
                //result = tcp_communicator.sendToOne(_leader.get_host().getHostManager()., tcp_replyMsg_one, signedMessage);
                break;
            case LeaderJobs.KEEPUPLOG:
                break;
            case LeaderJobs.APPENDLOG:
                break;
            case LeaderJobs.HEARTBEAT:
                break;
            case LeaderJobs.COMMITLOG:
                break;
        }
    }
}
