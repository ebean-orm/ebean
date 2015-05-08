package com.avaje.ebeaninternal.server.cluster.mcast;

import java.util.HashMap;
import java.util.List;

/**
 * For this node this holds the ACK gotAllPoint for each member in the cluster.
 * <p>
 * As we receive messages from other members of the cluster periodically we need
 * to send them ACK messages to say we got all the packets up to the gotAllPoint.
 * </p>
 * Thread Safety note: Object only used by McastClusterBroadcast Manager thread.
 * So Single Threaded access.
 * 
 * @author rbygrave
 */
public class IncomingPacketsLastAck {

    private HashMap<String,MessageAck> lastAckMap = new HashMap<String, MessageAck>();

    public String toString() {
        return lastAckMap.values().toString();
    }
    
    /**
     * Remove a member of the cluster who has left.
     */
    public void remove(String memberHostPort) {
        lastAckMap.remove(memberHostPort);
    }
    
    /**
     * Get the last Ack point for a given member of the cluster.
     */
    public MessageAck getLastAck(String memberHostPort) {
        return lastAckMap.get(memberHostPort);
    }
    
    /**
     * For the ACK messages in AckResendMessages update the
     * last Ack packetId.
     */
    public void updateLastAck(AckResendMessages ackResendMessages) {
        List<Message> messages = ackResendMessages.getMessages();
        for (int i = 0; i < messages.size(); i++) {
            Message msg = messages.get(i);
            if (msg instanceof MessageAck){
                MessageAck lastAck = (MessageAck)msg;
                lastAckMap.put(lastAck.getToHostPort(), lastAck);
            }
        }
    }
}
