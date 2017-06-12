package host;
import java.util.Observable;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by owen on 5/26/17.
 */
public class Follower extends Observable implements Runnable {
    // Receive heart beat, appendRPC, voteRPC, forwardRPC
    // Send forward, AppendRPCReply, VoteReply, voteRPC
    // follower will random time(300ms - 500ms) out and send the notify to the observer

    private StateManager _stateManager;
    private double _time;
    private final int BASELATENCY = 300;
    private final int DURATION = 200;
    private boolean _closed;

    public Follower(StateManager stateManager){
        resetTimer();
        _stateManager = stateManager;
    }

    @Override
    public void run() {
        System.out.println("I am Follower!");
        _closed = false;
        /*long startTime = System.currentTimeMillis();
        while(_time>0){
            long endTime   = System.currentTimeMillis();
            _time -= (endTime - startTime);
            startTime = endTime;
        }*/

        Timer _timer = new Timer();
        int duration = 10;
        _timer.schedule(new TimerTask() {
            @Override
            public void run() {
                _time -= duration;
                if(_time < 0 && !_closed){
                    //System.out.println("Changed!");
                    resetTimer();
                    _timer.cancel();
                    setChanged();
                    notifyObservers(CharacterManagement.F2C);
                }
            }
        }, 0, duration);
    }

    public void leave(){
        _closed = true;
    }

    /**
     * If host receive heartbeat msg will call this function.
     */
    public void receivedHeartBeat(){
        resetTimer();
    }

    public void appendAnEntry(State newState, int term){
        resetTimer();
        _stateManager.appendAnEntry(newState, term);
    }

    public boolean deleteLastEntry() {
        resetTimer();
        return _stateManager.deleteLastEntry();
    }

    public boolean commitLastEntry(){
        resetTimer();
        return _stateManager.commitLastEntry();
    }

    private void resetTimer(){
        _time = randomTimeout();
    }

    private int randomTimeout(){
        int result = ( int ) ( Math.random() * DURATION ) + BASELATENCY;
        //System.out.println("Follower Timer reset to " + result + " ms");
        return result;
    }

//    public static void main(String[] arg){
//        StateManager stateManager = new StateManager();
//        (new Thread(new Follower(stateManager))).start();
//    }
}
