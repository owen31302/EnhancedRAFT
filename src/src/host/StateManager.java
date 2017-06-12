package host;

import java.util.*;

/**
 * Created by TC_Yeh on 5/26/2017.
 */
public class StateManager {
    private ArrayList<LogEntry> stateLog;
    private HashMap<String, State> states;
    private Storage fileStoreHandler;
    private boolean commitFailEnable = false;
//    private String hostName;

    StateManager(String[] stateName, String hostName){
        stateLog = new ArrayList<LogEntry>(16);
        states = new HashMap<>();
        this.fileStoreHandler = new Storage(hostName);
        int i = 0;
        for (String name: stateName) {
            State initializedState = new State(name, 0);
            LogEntry initialLog = new LogEntry(initializedState, 0, i);
            states.put(name, initializedState);
            stateLog.add(initialLog);
            commitEntry(i);
            i++;
        }
//        this.hostName = hostName;
        // reconver logentry from disk
       // stateLog.addAll(Arrays.asList(fileStoreHandler.getAllCommitedValue()));
    }

    /**
     * This method commits the last entry in the entryLog
     * can set to not commit, representing hardware failure, demonstrating Raft
     * @return commit is success
     */
    public boolean commitLastEntry(){
        //if some node recover from crash, do they have a lot of un-commit entry?
        //assuming only newest log entry needs to be commit. older entry are committed.
        return commitEntry(stateLog.size() - 1);
    }

    public boolean commitEntry(int at) {
        LogEntry logToCommit = stateLog.get(at);
//        System.out.println(fileStoreHandler == null);
//        System.out.println(logToCommit.getState());
        if (logToCommit == null) {
            System.out.println("no found");
            return false;
        }
        if (commitFailEnable) {
            return false;
        }else if (fileStoreHandler.storeNewValue(logToCommit)){
            LogEntry temp = stateLog.get(at);
            //states.get(stateLog.get(at).getState().getStateName()).changeState(stateLog.get(at).getState().getStateValue());
            states.get(temp.getState().getStateName()).changeState(temp.getState().getStateValue());
            stateLog.get(at).commitEntry();
            return true;
        }else {
            return false;
        }
    }

    /**
     * This method adds a new state into log list
     *@param newState a new state to be logged
     *@param term term
     */
    public void appendAnEntry(State newState, int term){
        stateLog.add(new LogEntry(newState, term, stateLog.size()));
    }

    /**
     * This method would delete the last log entry
     * used by the newly leader to sync its log entries
     */
    public boolean deleteLastEntry() {
        if (stateLog.isEmpty()) {
            return true;
        }
        if (fileStoreHandler.deleteLatestCommitedValue()) {
            stateLog.remove(stateLog.size()-1);
            return true;
        }else {
            return false;
        }
    }


    /**
     * Let the follower not to commit the entry
     */
    public void enableCommitFail() {
        this.commitFailEnable = true;
    }
    /**
     * Let the follower act normal
     */
    public void disableCommitFail() {
        this.commitFailEnable = false;
    }

    public LogEntry getLastLog(){
        return stateLog.get(stateLog.size()-1);
    }

    public int getLastIndex(){
        return stateLog.size()-1;
    }
    public LogEntry getLog(int index){

        return stateLog.get(index);
    }

    public String toString(){
        StringBuffer result = new StringBuffer();
        for (Map.Entry<String, State> a: states.entrySet()) {
            result.append(a.getValue().toString() + ";");
        }
        return result.toString().substring(0, result.length()-1);
    }
}
