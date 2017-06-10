package host;

import Communicator.TCP_Communicator;
import Communicator.TCP_ReplyMsg_One;
import signedMethods.SignedMessage;

import javax.annotation.processing.SupportedSourceVersion;

/**
 * Created by Yu-Cheng Lin on 5/31/17.
 */
public class Leader_Worker implements Runnable {

    private String _hostName;
    private int _leaderJob;
    private Leader _leader;
    private Host _host;

    public Leader_Worker(Leader leader, int leaderJob, String hostName, Host host){
        _leader = leader;
        _leaderJob = leaderJob;
        _hostName = hostName;
        _host = host;
    }

    @Override
    public void run() {
        SignedMessage signedMessage;
        TCP_Communicator tcp_communicator = new TCP_Communicator();
        TCP_ReplyMsg_One tcp_replyMsg_one = new TCP_ReplyMsg_One();
        int index;
        LogEntry prelogEntry;
        LogEntry curlogEntry;
        String appendEntry;
        boolean result;

        String[] appendEntryArray = new String[]{
                _host.getCurrentTerm().toString(),
                _host.getHostName(),
                String.valueOf(_host.getStateManager().getLastLog().getIndex()),
                String.valueOf(_host.getStateManager().getLastLog().getTerm()),
                "",
                String.valueOf(_host.getCommitIndex())
        };

        switch (_leaderJob){
            case LeaderJobs.FINDINDEX:
                System.out.println("LeaderJobs.FINDINDEX");
                // new 一個thread，然後去append看看是否成功，如果成功代表我找到相同位置
                // 沒有成功，index要decrement，然後再試一次
                index = _leader.get_nextIndex().get(_hostName);
                prelogEntry = _leader.get_host().getStateManager().getLog(index-1);
                appendEntryArray[2] = String.valueOf(prelogEntry.getIndex());
                appendEntryArray[3] = String.valueOf(prelogEntry.getTerm());
                appendEntry = String.join(",", appendEntryArray);
                signedMessage = new SignedMessage(RPCs.APPENDENTRY, appendEntry, _leader.get_host().getPrivateKey());
                result = tcp_communicator.initSendToOne(_leader.get_host().getHostManager().getHostAddress(_hostName), tcp_replyMsg_one, signedMessage);
                if(result) {
                    if (tcp_replyMsg_one.getMessage().equals(RPCs.SUCCESS)) {
                        System.out.println("RPCs.SUCCESS");
                        _leader.get_isFindNextIndex().add(_hostName);
                    }else if(tcp_replyMsg_one.getMessage().equals(RPCs.FAIL)){
                        System.out.println("RPCs.FAIL");
                        int nextIndex = _leader.get_nextIndex().get(_hostName);
                        _leader.get_nextIndex().put(_hostName, nextIndex - 1);
                    }else{
                        System.out.println("Some error.");
                    }
                }
                break;
            case LeaderJobs.KEEPUPLOG:
                System.out.println("LeaderJobs.KEEPUPLOG");
                // 找到相同位置後，如果不為最新，則要慢慢追上
                index = _leader.get_nextIndex().get(_hostName);
                prelogEntry = _leader.get_host().getStateManager().getLog(index-1);
                curlogEntry = _leader.get_host().getStateManager().getLog(index);
                appendEntryArray[2] = String.valueOf(prelogEntry.getIndex());
                appendEntryArray[3] = String.valueOf(prelogEntry.getTerm());
                appendEntryArray[4] = curlogEntry.getState().toString();
                appendEntry = String.join(",", appendEntryArray);
                signedMessage = new SignedMessage(RPCs.APPENDENTRY, appendEntry, _leader.get_host().getPrivateKey());
                result = tcp_communicator.initSendToOne(_leader.get_host().getHostManager().getHostAddress(_hostName), tcp_replyMsg_one, signedMessage);
                if(result){
                    if (tcp_replyMsg_one.getMessage().equals(RPCs.SUCCESS)) {
                        System.out.println("RPCs.SUCCESS");
                        _leader.get_nextIndex().put(_hostName, index+1);
                    }else if(tcp_replyMsg_one.getMessage().equals(RPCs.FAIL)){
                        System.out.println("RPCs.FAIL");
                        _leader.get_isFindNextIndex().remove(_hostName);
                    }else{
                        System.out.println("Some error.");
                    }
                }
                break;
            case LeaderJobs.APPENDLOG:
                System.out.println("LeaderJobs.APPENDLOG");
                // 前面兩個都通過了，才會執行user request
                // 如果投票一直沒過，follower就不斷覆蓋同個位置上的log
                index = _leader.get_host().getCommitIndex()+1;
                prelogEntry = _leader.get_host().getStateManager().getLog(index-1);
                State state = _leader.getState();
                appendEntryArray[2] = String.valueOf(prelogEntry.getIndex());
                appendEntryArray[3] = String.valueOf(prelogEntry.getTerm());
                appendEntryArray[4] = state.toString();
                appendEntry = String.join(",", appendEntryArray);
                signedMessage = new SignedMessage(RPCs.APPENDENTRY, appendEntry, _leader.get_host().getPrivateKey());
                result = tcp_communicator.initSendToOne(_leader.get_host().getHostManager().getHostAddress(_hostName), tcp_replyMsg_one, signedMessage);
                if(result){
                    if (tcp_replyMsg_one.getMessage().equals(RPCs.SUCCESS)) {
                        System.out.println("RPCs.SUCCESS");
                        _leader.set_votes();
                    }else if(tcp_replyMsg_one.getMessage().equals(RPCs.FAIL)){
                        System.out.println("RPCs.FAIL");
                        _leader.get_isFindNextIndex().remove(_hostName);
                    }else{
                        System.out.println("Some error.");
                    }

                }
                break;
            case LeaderJobs.HEARTBEAT:
                System.out.println("LeaderJobs.HEARTBEAT");
                // 如果沒新東西就heartbeat
                appendEntry = String.join(",", appendEntryArray);
                signedMessage = new SignedMessage(RPCs.HEARTBEAT, appendEntry, _leader.get_host().getPrivateKey());
                result = tcp_communicator.initSendToOne(_leader.get_host().getHostManager().getHostAddress(_hostName), tcp_replyMsg_one, signedMessage);
                if(result){
                    if (tcp_replyMsg_one.getMessage().equals(RPCs.SUCCESS)) {
                        System.out.println("RPCs.SUCCESS");
                    }else if(tcp_replyMsg_one.getMessage().equals(RPCs.FAIL)){
                        System.out.println("RPCs.FAIL");
                        _leader.get_isFindNextIndex().remove(_hostName);
                    }else{
                        System.out.println("Some error.");
                    }
                }
                break;
        }
    }
}
