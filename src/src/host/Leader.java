package host;

import Communicator.TCP_Communicator;
import Communicator.TCP_ReplyMsg_All;
import signedMethods.SignedMessage;

import java.util.*;

/**
 * Created by TC_Yeh on 5/26/2017.
 */
public class Leader extends Observable implements Runnable {
    private boolean _closed;
    private HashMap<String , Integer> _nextIndex;
    private HashSet<String> _isFindNextIndex;
    private Host _host;
    private TCP_ReplyMsg_All _tcp_replyMsg_all;
    private Set<String> _hostnames;
    private int _votes;
    public Queue<BState> _queue;
    public Lock _Lock;

    public Leader(Host host, TCP_ReplyMsg_All tcp_replyMsg_all){
        _Lock = new Lock();
        _queue = new LinkedList<>();
        _host = host;
        _tcp_replyMsg_all = tcp_replyMsg_all;
        _nextIndex = new HashMap<>();
        _isFindNextIndex = new HashSet<>();
    }

    @Override
    public void run() {

        System.out.println("I am Leader!");
        _host.getHostManager().setLeaderAddress(_host.getHostName());
        // (1)  get the latest log index for each host
        //      update the log for non-up-to-date hosts
        //      1. initial each log index to the latest index (done in constructor)
        //      2. find the matching index and append the remaining logs
        // (2)  append the user request
        // (3)  send the heartbeat
        // (1), (2), (3) will happen concurrently.

        SignedMessage signedMessage;
        TCP_Communicator tcp_communicator = new TCP_Communicator();
        _hostnames = _host.getHostManager().getHostNames();
        int lastIndex = _host.getCommitIndex();
        for(String hostname : _hostnames){
            _nextIndex.put(hostname, lastIndex + 1);
        }



        _closed = false;
        while (!_closed){
            _votes = 1;
            HashMap<String, Thread> threads = new HashMap<>();
            int queueSize = _queue.size();
            boolean appendFlag = false;
            for(String hostname : _hostnames){
                if(!hostname.equals(_host.getHostManager().getMyHostName())){
                    if(!_isFindNextIndex.contains(hostname)){
                        // new 一個thread，然後去append看看是否成功，如果成功代表我找到相同位置
                        // 沒有成功，index要decrement，然後再試一次
                        threads.put(hostname, new Thread(new Leader_Worker(this, LeaderJobs.FINDINDEX, hostname, _host)));
                    }else if(_nextIndex.get(hostname) <= _host.getCommitIndex()){
                        // 找到相同位置後，如果不為最新，則要慢慢追上
                        threads.put(hostname, new Thread(new Leader_Worker(this, LeaderJobs.KEEPUPLOG, hostname, _host)));
                    }else if(queueSize > 0){
                        // 前面兩個都通過了，才會執行user request
                        // 如果投票一直沒過，follower就不斷覆蓋同個位置上的log
                        appendFlag = true;
                        threads.put(hostname, new Thread(new Leader_Worker(this, LeaderJobs.APPENDLOG, hostname, _host)));
                    }else{
                        // 如果沒新東西就heartbeat
                        threads.put(hostname, new Thread(new Leader_Worker(this, LeaderJobs.HEARTBEAT, hostname, _host)));
                    }
                    threads.get(hostname).setDaemon(true);
                    threads.get(hostname).start();
                }
            }
            // 等所有thread都做完事情
            for(String hostname : _hostnames){
                if(!hostname.equals(_host.getHostManager().getMyHostName())){
                    try{
                        threads.get(hostname).join();
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }
            }
            // 計算append log return true是否超過半數
            // 是，則更新自己的commit，下個while loop會更新follower的log
            // 否，再繼續while loop
            if(appendFlag){
                boolean result = _votes > _hostnames.size() / 2;
                if(result){
                    //System.out.println("Commit 1");
                    _host.setCommitIndex(_host.getCommitIndex()+1);
                    _host.getStateManager().commitEntry(_host.getCommitIndex());
                    System.out.println(_host.getStateManager().toString());
                }else{
                    //System.out.println("deleteLastEntry 1");
                    _host.getStateManager().deleteLastEntry();
                }
                synchronized (_Lock){
                    _Lock._result = result;
                    _Lock.notifyAll();
                }
            }
        }
    }

    public void set_votes(){
        _votes++;
    }
    public Host get_host(){
        return _host;
    }
    public HashSet<String> get_isFindNextIndex(){
        return _isFindNextIndex;
    }
    public HashMap<String , Integer> get_nextIndex(){
//        for(Map.Entry<String, Integer> a : _nextIndex.entrySet()){
//            System.out.println("hostName:" + a.getKey() + " index:" + a.getValue());
//        }
        return _nextIndex;
    }

    public boolean addState(State state, boolean isByzantine){
        BState bState = new BState(state, isByzantine);
        _queue.offer(bState);
        synchronized (_Lock){
            try {
                _Lock.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //System.out.println("_Lock._result: " + _Lock._result);
            return _Lock._result;
        }
    }

    public BState getState(){
        return _queue.poll();
    }

    class Lock{
        public boolean _result;

        public Lock(){
            _result = false;
        }
    }

    public void leave(){
        _closed = true;
    }

    class BState{
        public State _state;
        public boolean _isByzantine;
        public BState(State state, boolean isByzantine){
            _state = state;
            _isByzantine = isByzantine;
        }
    }
}
