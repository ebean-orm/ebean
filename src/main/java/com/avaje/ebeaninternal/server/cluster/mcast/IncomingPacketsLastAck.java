/**
 * Copyright (C) 2009 Authors
 * 
 * This file is part of Ebean.
 * 
 * Ebean is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *  
 * Ebean is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Ebean; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA  
 */
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
