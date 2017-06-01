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
                // new 一個thread，然後去append看看是否成功，如果成功代表我找到相同位置
                // 沒有成功，index要decrement，然後再試一次
                int index = _leader.get_nextIndex().get(_hostName);
                LogEntry logEntry = _leader.get_host().getStateManager().getLog(index);
                signedMessage = new SignedMessage(RPCs.APPENDENTRY, logEntry.getString(), _leader.get_host().getPrivateKey());
                boolean result = tcp_communicator.sendToOne(_leader.get_host().getHostManager().getHostAddress(_hostName), tcp_replyMsg_one, signedMessage);
                if(result){
                    _leader.get_findNextIndex().add(_hostName);
                }else{
                    int nextIndex = _leader.get_nextIndex().get(_hostName);
                    _leader.get_nextIndex().put(_hostName, nextIndex-1);
                }
                break;
            case LeaderJobs.KEEPUPLOG:
                // 找到相同位置後，如果不為最新，則要慢慢追上
                break;
            case LeaderJobs.APPENDLOG:
                // 前面兩個都通過了，才會執行user request
                // 如果投票一直沒過，follower就不斷覆蓋同個位置上的log
                break;
            case LeaderJobs.HEARTBEAT:
                // 如果沒新東西就heartbeat
                break;
            case LeaderJobs.COMMITLOG:
                // 送Commit
                break;
        }
    }
}
