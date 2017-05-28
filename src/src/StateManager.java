import sun.rmi.runtime.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by TC_Yeh on 5/26/2017.
 */
public class StateManager {
    HashMap<String, State> states; //states is the set of state for this host
    ArrayList<LogEntry> stateLog; //logs of this host

    /**
     * This construct builds stateManager with input states.
     * @param states states of this host
     */
    StateManager(State[] states){
        stateLog = new ArrayList<LogEntry>();
        this.states = new HashMap<>(states.length);

        for (State a: states){
            this.states.put(a.getStateName(), a);
        }
    }

    /**
     * This constructor builds stateManager with state names and sets states values to zero.
     * @param stateNames names of states in this host
     */
    StateManager(String[] stateNames){
        stateLog = new ArrayList<LogEntry>();
        states = new HashMap<>(stateNames.length);

        for (String a: stateNames){
            states.put(a, new State(a, 0));
        }
    }

    /**
     * This method commits the last LogEntry in the log and update the states of this host.
     */
    public void commitAnEntry(){ //if some node recover from crash, do they have a lot of un-commit entry?
        //assuming only newest log entry needs to be commit. older entry are committed.
        int size = states.size();
        stateLog.get(size - 1).commitEntry();
        State newestState = stateLog.get(size - 1).getState();
        states.get(newestState.getStateName()).changeState(newestState.getStateValue());
    }

    /**
     * This method logs a new state
     * @param newState new state to be logged
     * @param term current host term
     */
    public void appendEntry(State newState, int term){
        stateLog.add(new LogEntry(newState, term, stateLog.size()));
    }
}
