package com.avaje.ebeaninternal.server.cluster.mcast;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebeaninternal.server.cluster.mcast.IncomingPacketsProcessed.GotAllPoint;

public class TestMcastMemberPackets extends BaseTestCase {

  @Test
  public void test() {

    GotAllPoint member = new GotAllPoint("129.12.23.12:9089", 3);

    Assert.assertTrue(member.processPacket(1234));
    Assert.assertTrue(member.processPacket(1235));
    Assert.assertTrue(member.processPacket(1236));

    Assert.assertEquals(1236l, member.getGotAllPoint());
    Assert.assertEquals(0, member.getMissingPackets().size());

    Assert.assertFalse(member.processPacket(1234));

    Assert.assertTrue(member.processPacket(1239));
    List<Long> missingPackets = member.getMissingPackets();
    Assert.assertEquals(2, missingPackets.size());

    Assert.assertTrue(missingPackets.contains(1237l));
    Assert.assertTrue(missingPackets.contains(1238l));
    Assert.assertFalse(missingPackets.contains(1239l));
    Assert.assertFalse(missingPackets.contains(1236l));

    missingPackets = member.getMissingPackets();
    Assert.assertEquals(2, missingPackets.size());
    Assert.assertTrue(missingPackets.contains(1237l));
    Assert.assertTrue(missingPackets.contains(1238l));

    Assert.assertEquals(1236l, member.getGotAllPoint());

    // get a missing packet
    Assert.assertTrue(member.processPacket(1237));
    Assert.assertEquals(1237l, member.getGotAllPoint());

    missingPackets = member.getMissingPackets();
    Assert.assertEquals(1, missingPackets.size());
    Assert.assertTrue(missingPackets.contains(1238l));

    // but we now hit maxResendIncoming
    missingPackets = member.getMissingPackets();
    Assert.assertEquals(0, missingPackets.size());
    // gave up on 1238 ..
    Assert.assertEquals(1239l, member.getGotAllPoint());

  }

}
