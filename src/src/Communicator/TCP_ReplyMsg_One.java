package Communicator;

import signedMethods.SignedMessage;

/**
 * Created by shan on 5/29/17.
 */
public class TCP_ReplyMsg_One {
    private SignedMessage message;

    /**
     * Get message
     * @return
     */
    public SignedMessage getMessage() {
        return message;
    }

    /**
     * Set message to given message
     * @param message
     */
    public void setMessage(SignedMessage message) {
        this.message = message;
    }

}
