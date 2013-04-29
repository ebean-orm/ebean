package com.avaje.ebeaninternal.server.cluster.mcast;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;

public class TestPacketsAcked extends BaseTestCase {

  @Test
  public void test() {

    OutgoingPacketsAcked packetsAcked = new OutgoingPacketsAcked();

    Assert.assertEquals(0l, packetsAcked.getMinimumGotAllPacketId());

    long receivedAck = packetsAcked.receivedAck("A", new MessageAck("A", 1020l));
    Assert.assertEquals(1020l, packetsAcked.getMinimumGotAllPacketId());
    Assert.assertEquals(1020l, receivedAck);

    receivedAck = packetsAcked.receivedAck("B", new MessageAck("B", 1030l));
    Assert.assertEquals(1020l, packetsAcked.getMinimumGotAllPacketId());
    Assert.assertEquals(0l, receivedAck);

    receivedAck = packetsAcked.receivedAck("C", new MessageAck("C", 1025l));
    Assert.assertEquals(0l, receivedAck);

    receivedAck = packetsAcked.receivedAck("A", new MessageAck("A", 1040l));
    Assert.assertEquals(1025l, receivedAck);

  }
}
