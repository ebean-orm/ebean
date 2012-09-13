package com.avaje.ebeaninternal.server.cluster.mcast;

import java.util.List;

import junit.framework.TestCase;

import com.avaje.ebeaninternal.server.cluster.mcast.IncomingPacketsProcessed.GotAllPoint;

public class TestMcastMemberPackets extends TestCase {

    public void test() {
        
        GotAllPoint member = new GotAllPoint("129.12.23.12:9089",3);
        
        assertTrue(member.processPacket(1234));
        assertTrue(member.processPacket(1235));
        assertTrue(member.processPacket(1236));
        
        assertEquals(1236l, member.getGotAllPoint());
        assertEquals(0, member.getMissingPackets().size());
     
        assertFalse(member.processPacket(1234));
        
        assertTrue(member.processPacket(1239));
        List<Long> missingPackets = member.getMissingPackets();
        assertEquals(2, missingPackets.size());
        
        assertTrue(missingPackets.contains(1237l));
        assertTrue(missingPackets.contains(1238l));
        assertFalse(missingPackets.contains(1239l));
        assertFalse(missingPackets.contains(1236l));

        missingPackets = member.getMissingPackets();
        assertEquals(2, missingPackets.size());
        assertTrue(missingPackets.contains(1237l));
        assertTrue(missingPackets.contains(1238l));

        assertEquals(1236l, member.getGotAllPoint());
        
        // get a missing packet
        assertTrue(member.processPacket(1237));
        assertEquals(1237l, member.getGotAllPoint());
        
        missingPackets = member.getMissingPackets();
        assertEquals(1, missingPackets.size());
        assertTrue(missingPackets.contains(1238l));


        // but we now hit maxResendIncoming
        missingPackets = member.getMissingPackets();
        assertEquals(0, missingPackets.size());
        // gave up on 1238 ..
        assertEquals(1239l, member.getGotAllPoint());

        
    }
    
}
