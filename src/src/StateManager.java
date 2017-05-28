import java.io.IOException;
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
        try{
            this.fileStoreHandler = new Storage("storedValue");
        }catch (IOException exception) {
            System.out.println("creating StateManager");
            exception.printStackTrace();
        }

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
    public void deleteLastEntry() {
        if (stateLog.isEmpty()) {
            return;
        }

        LogEntry logToDelete = stateLog.removeLast();

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
}
