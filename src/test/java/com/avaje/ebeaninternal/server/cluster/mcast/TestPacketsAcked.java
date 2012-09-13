package com.avaje.ebeaninternal.server.cluster.mcast;

import junit.framework.TestCase;

public class TestPacketsAcked extends TestCase {

    public void test() {
        
        OutgoingPacketsAcked packetsAcked = new OutgoingPacketsAcked();
        
        assertEquals(0l,packetsAcked.getMinimumGotAllPacketId());
        
        long receivedAck = packetsAcked.receivedAck("A", new MessageAck("A", 1020l));
        assertEquals(1020l,packetsAcked.getMinimumGotAllPacketId());
        assertEquals(1020l,receivedAck);

        receivedAck = packetsAcked.receivedAck("B", new MessageAck("B", 1030l));
        assertEquals(1020l,packetsAcked.getMinimumGotAllPacketId());
        assertEquals(0l,receivedAck);

        receivedAck = packetsAcked.receivedAck("C", new MessageAck("C", 1025l));
        assertEquals(0l,receivedAck);
        
        receivedAck = packetsAcked.receivedAck("A", new MessageAck("A", 1040l));
        assertEquals(1025l,receivedAck);

    }
}
