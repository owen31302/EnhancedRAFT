import java.io.IOException;
import java.net.ServerSocket;

/**
 * Created by TC_Yeh on 5/26/2017.
 */
public class Host {
    private StateManager stateManager;
    //need to store public keys of other hosts
    private ServerSocket aServer;
    private Leader leader;
    private Follower follower;
    private Candidate candidate;

    Host() throws IOException {
        stateManager = new StateManager();
        aServer = new ServerSocket();
        leader = new Leader();
        follower = new Follower();
        candidate = new Candidate();

    }


    static public void main(String args[]){

    }


}
