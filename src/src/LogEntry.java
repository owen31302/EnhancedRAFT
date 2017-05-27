/**
 * Created by TC_Yeh on 5/26/2017.
 */
public class LogEntry {
    private State state;
    private int term;
    private int index; // index maybe not needed
    private boolean isCommitted;

    /**
     * This constructor builds a LogEntry with input data and set isCommitted field false.
     * @param aState aState
     * @param term term
     * @param index index
     */
    LogEntry(State aState, int term, int index){
        //the input should be the host's current state, we need a copy of it.
        //otherwise, state in this class will change will state in host.
        this.state = aState.clone();
        this.term = term;
        this.index = index;
        isCommitted = false;
    }

    /**
     * This method returns state of LogEntry
     * @return State
     */
    public State getState(){
        return state;
    }

    /**
     * This method returns term of LogEntry
     * @return term
     */
    public int getTerm(){
        return term;
    }

    /**
     * This method returns index of LogEntry
     * @return index
     */
    public int getIndex(){
        return index;
    }

    /**
     * To check is this LogEntry committed
     * @return isCommitted
     */
    public boolean isCommitted(){
        return isCommitted;
    }

    /**
     * This method sets isCommitted to true
     */
    public void commitEntry(){
        isCommitted = true;
    }

    /**
     * This method compares two LogEntry, returns true if state, term and index are all same. Ignore isCommitted
     * @param anEntry anEntry
     * @return boolean
     */
    public boolean equals(LogEntry anEntry){
        return (state.equals(anEntry.getState()) && term == anEntry.getTerm() && index == anEntry.getIndex());
    }

}
