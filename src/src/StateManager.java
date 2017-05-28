import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by TC_Yeh on 5/26/2017.
 */
public class StateManager {
    LinkedList<LogEntry> stateLog;
    private Storage fileStoreHandler;
    private boolean commitFailEnable = false;

    StateManager(){
        stateLog = new LinkedList<LogEntry>();
        this.fileStoreHandler = new Storage("storedValue");
    }

    public boolean commitAnEntry(){
        //if some node recover from crash, do they have a lot of un-commit entry?
        //assuming only newest log entry needs to be commit. older entry are committed.
        LogEntry logToCommit = stateLog.getLast();
        if (logToCommit == null) {
            return false;
        }

        if (commitFailEnable) {
            return false;
        }else if (fileStoreHandler.storeNewValue(logToCommit)){
            stateLog.getLast().commitEntry();
            return true;
        }else {
            return false;
        }
    }
    public void enableCommitFail() {
        this.commitFailEnable = true;
    }
    public void disableCommitFail() {
        this.commitFailEnable = false;
    }

    /**
     * This method adds a new state into log list
     *@param newState a new state to be logged
     *@param term term
     */
    public void appendAnEntry(State newState, int term){
        stateLog.add(new LogEntry(newState, term, stateLog.size()));
    }
}
