package host;

import Communicator.TCP_Communicator;
import Communicator.TCP_ReplyMsg_One;
import signedMethods.SignedMessage;

import javax.annotation.processing.SupportedSourceVersion;
import java.security.interfaces.RSAPublicKey;

/**
 * Created by Yu-Cheng Lin on 5/31/17.
 */
public class Leader_Worker implements Runnable {

    private String _hostName;
    private int _leaderJob;
    private Leader _leader;
    private Host _host;
    private boolean commentFlag = false;

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
        RSAPublicKey rsaPublicKey;
        String msg;

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
                if (commentFlag){
                    System.out.println("LeaderJobs.FINDINDEX");
                }
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
                    rsaPublicKey = _host.getHostManager().getPublicKey(_hostName);
                    msg = tcp_replyMsg_one.getMessage().getPlanText(rsaPublicKey);
                    if (msg.equals(RPCs.SUCCESS)) {
                        if (commentFlag){
                            System.out.println("RPCs.SUCCESS 1");
                        }
                        _leader.get_isFindNextIndex().add(_hostName);
                    }else if(msg.equals(RPCs.FAIL)){
                        if (commentFlag){
                            System.out.println("RPCs.FAIL 1");
                        }
                        int nextIndex = _leader.get_nextIndex().get(_hostName);
                        _leader.get_nextIndex().put(_hostName, nextIndex - 1);
                    }else{
                        if (commentFlag){
                            System.out.println("Some error 1");
                        }
                    }
                }else {
                    System.out.println("Result False 1");
                }
                break;
            case LeaderJobs.KEEPUPLOG:
                if (commentFlag){
                    System.out.println("LeaderJobs.KEEPUPLOG");
                }
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
                    rsaPublicKey = _host.getHostManager().getPublicKey(_hostName);
                    msg = tcp_replyMsg_one.getMessage().getPlanText(rsaPublicKey);
                    if (msg.equals(RPCs.SUCCESS)) {
                        if (commentFlag){
                            System.out.println("RPCs.SUCCESS 2");
                        }
                        _leader.get_nextIndex().put(_hostName, index+1);
                    }else if(msg.equals(RPCs.FAIL)){
                        if (commentFlag){
                            System.out.println("RPCs.FAIL 2");
                        }
                        _leader.get_isFindNextIndex().remove(_hostName);
                    }else{
                        if (commentFlag){
                            System.out.println("Some error 2");
                        }
                    }
                }else {
                    System.out.println("Result False 2");
                }
                break;
            case LeaderJobs.APPENDLOG:
                if (commentFlag){
                    System.out.println("LeaderJobs.APPENDLOG");
                }
                // 前面兩個都通過了，才會執行user request
                // 如果投票一直沒過，follower就不斷覆蓋同個位置上的log
                State state = null;
                synchronized (_leader._queue){
                    if(!_leader._queue.isEmpty()){
                        state = _leader.getState();
                        _host.getStateManager().appendAnEntry(state, _host.getCurrentTerm());
                    }else{
                        state = _host.getStateManager().getLastLog().getState();
                        appendEntryArray[2] = String.valueOf(_host.getStateManager().getLog(_host.getStateManager().getLastIndex()-1).getIndex());
                        appendEntryArray[3] = String.valueOf(_host.getStateManager().getLog(_host.getStateManager().getLastIndex()-1).getTerm());
                    }
                }
                appendEntryArray[4] = state.toString();
                appendEntry = String.join(",", appendEntryArray);
                signedMessage = new SignedMessage(RPCs.APPENDENTRY, appendEntry, _leader.get_host().getPrivateKey());
                result = tcp_communicator.initSendToOne(_leader.get_host().getHostManager().getHostAddress(_hostName), tcp_replyMsg_one, signedMessage);
                if(result){
                    rsaPublicKey = _host.getHostManager().getPublicKey(_hostName);
                    msg = tcp_replyMsg_one.getMessage().getPlanText(rsaPublicKey);
                    if (msg.equals(RPCs.SUCCESS)) {
                        if (commentFlag){
                            System.out.println("RPCs.SUCCESS 3");
                        }
                        int currentIndex = _leader.get_nextIndex().get(_hostName);
                        _leader.get_nextIndex().put(_hostName, currentIndex+1);
                        _leader.set_votes();
                    }else if(msg.equals(RPCs.FAIL)){
                        if (commentFlag){
                            System.out.println("RPCs.FAIL 3");
                        }
                        _leader.get_isFindNextIndex().remove(_hostName);
                    }else{
                        if (commentFlag){
                            System.out.println("Some error 3");
                        }
                    }
                }else {
                    System.out.println("Result False 3");
                }
                break;
            case LeaderJobs.HEARTBEAT:
                if (commentFlag){
                    System.out.println("LeaderJobs.HEARTBEAT");
                }
                // 如果沒新東西就heartbeat
                appendEntry = String.join(",", appendEntryArray);
                signedMessage = new SignedMessage(RPCs.APPENDENTRY, appendEntry, _leader.get_host().getPrivateKey());
                result = tcp_communicator.initSendToOne(_leader.get_host().getHostManager().getHostAddress(_hostName), tcp_replyMsg_one, signedMessage);
                if(result){
                    rsaPublicKey = _host.getHostManager().getPublicKey(_hostName);
                    msg = tcp_replyMsg_one.getMessage().getPlanText(rsaPublicKey);
                    if (msg.equals(RPCs.SUCCESS)) {
                        if (commentFlag){
                            System.out.println("RPCs.SUCCESS 4");
                        }
                    }else if(msg.equals(RPCs.FAIL)){
                        if (commentFlag){
                            System.out.println("RPCs.FAIL 4");
                        }
                        _leader.get_isFindNextIndex().remove(_hostName);
                    }else{
                        if (commentFlag){
                            System.out.println("Some error 4");
                        }
                    }
                }else {
                    System.out.println("Result False 4");
                }
                break;
        }
    }
}
