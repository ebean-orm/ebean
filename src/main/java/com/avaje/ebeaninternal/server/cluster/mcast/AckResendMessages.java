package com.avaje.ebeaninternal.server.cluster.mcast;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds a list of ACK and RESEND messages that should be sent out.
 * 
 * @author rbygrave
 */
public class AckResendMessages {

    ArrayList<Message> messages = new ArrayList<Message>();
    
    public String toString() {
        return messages.toString();
    }
    
    public int size() {
        return messages.size();
    }
    
    /**
     * Add a ACK message to send.
     */
    public void add(MessageAck ack){
        messages.add(ack);
    }
    
    /**
     * Add a RESEND message to send.
     */
    public void add(MessageResend resend){
        messages.add(resend);
    }
    
    /**
     * Return all the messages to be sent out.
     */
    public List<Message> getMessages() {
        return messages;
    }
}
