/**
 * Created by TC_Yeh on 5/26/2017.
 */
public class LogEntry {
    private int term;
    private int stateX;
    private int stateY;
    private int stateZ;
    private boolean isCommit;

    LogEntry(int term, int x, int y, int z){
        this.term = term;
        stateX = x;
        stateY = y;
        stateZ = z;
        isCommit = false;
    }

    public int getStateX(){
        return stateX;
    }

    public int getStateY(){
        return stateY;
    }

    public int getStateZ(){
        return stateZ;
    }

    public int getTerm(){
        return term;
    }
    public void setStateX(int x){
        stateX = x;
    }

    public void setStateY(int y){
        stateY = y;
    }

    public void setStateZ(int z){
        stateZ = z;
    }

    public void setTerm(int term){
        this.term = term;
    }

    public void commitEntry(){
        isCommit = true;
    }

    public boolean isCommitted(){
        return isCommit;
    }
}
