package Communicator;

import signedMethods.SignedMessage;

/**
 * Created by shan on 5/29/17.
 */
public class TCP_ReplyMsg_One {
    private SignedMessage message;

    public SignedMessage getMessage() {
        return message;
    }

    public void setMessage(SignedMessage message) {
        this.message = message;
    }

}
