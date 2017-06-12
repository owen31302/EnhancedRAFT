package host;

import signedMethods.SignedMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by TC_Yeh on 6/11/2017.
 */
public class ForwardCollector {
    HostManager hostManager;
    HashMap<String, Integer> forwardMsg;
   // boolean endOfTurn = false;

    /**
     * This class is used for collect msg forwarded from other followers.
     * @param hostManager
     */
    ForwardCollector(HostManager hostManager){
        this.hostManager = hostManager;
        forwardMsg = new HashMap<>();
    }

    /**
     * Put received signed message into collection list.
     * @param signedMessage
     */
    public synchronized void putIntoCollection(SignedMessage signedMessage){
        String plantext = signedMessage.getPlanText(hostManager.getLeaderPublicKey());
        if (forwardMsg.containsKey(plantext)) {
            forwardMsg.put(plantext, forwardMsg.get(plantext)+1 );
          //  System.out.println("put 1");
        }
        else {
         //   System.out.println("put 2");
            forwardMsg.put(plantext, 1);
        }
    }

    public synchronized String getResult(){
        int majoritySize = (hostManager.getHostList().size() - 1) / 2 + 1; // majority of followers, leader is not included
        for (Map.Entry<String, Integer> a: forwardMsg.entrySet()) {
            if (a.getValue() >= majoritySize) {
             //   System.out.println("max: " + a.getValue());
                forwardMsg = new HashMap<>();
                return a.getKey();
            }
        }
     //   System.out.println("not find");
        forwardMsg = new HashMap<>(); //clean old data;

        return null;
    }



}
