import java.util.LinkedList;

/**
 * Created by TC_Yeh on 5/26/2017.
 */
public class StateManager {
    LinkedList<LogEntry> stateLog;

    StateManager(){
        stateLog = new LinkedList<>();
    }

    public void commitAnEntry(){ //if some node recover from crash, do they have a lot of un-commit entry?
        //assuming only newest log entry needs to be commit. older entry are committed.
        stateLog.getLast().commitEntry();
    }

}
