package com.avaje.ebeaninternal.server.lib.sql;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;

public class TestBusyBuffer extends BaseTestCase {

  @Test
  public void test() {

    BusyConnectionBuffer b = new BusyConnectionBuffer(2, 4);

    PooledConnection p0 = new PooledConnection("0");
    PooledConnection p1 = new PooledConnection("1");
    PooledConnection p2 = new PooledConnection("2");
    PooledConnection p3 = new PooledConnection("3");

    Assert.assertEquals(2, b.getCapacity());
    b.add(p0);
    b.add(p1);
    Assert.assertEquals(2, b.getCapacity());
    b.add(p2);
    Assert.assertEquals(6, b.getCapacity());
    b.add(p3);

    Assert.assertEquals(0, p0.getSlotId());
    Assert.assertEquals(1, p1.getSlotId());
    Assert.assertEquals(2, p2.getSlotId());
    Assert.assertEquals(3, p3.getSlotId());

    b.remove(p2);
    b.add(p2);
    Assert.assertEquals(4, p2.getSlotId());

    b.remove(p0);
    b.add(p0);
    Assert.assertEquals(5, p0.getSlotId());

    b.remove(p2);
    b.add(p2);
    Assert.assertEquals(0, p2.getSlotId());

  }

  @Test
  public void test_rotate() {

    BusyConnectionBuffer b = new BusyConnectionBuffer(2, 2);

    PooledConnection p0 = new PooledConnection("0");
    PooledConnection p1 = new PooledConnection("1");
    PooledConnection p2 = new PooledConnection("2");
    PooledConnection p3 = new PooledConnection("3");

    Assert.assertEquals(2, b.getCapacity());
    Assert.assertEquals(0, b.size());

    b.add(p0);
    b.add(p1);
    Assert.assertEquals(2, b.size());
    Assert.assertEquals(2, b.getCapacity());
    b.add(p2);
    Assert.assertEquals(3, b.size());
    Assert.assertEquals(4, b.getCapacity());
    b.add(p3);
    Assert.assertEquals(4, b.size());
    Assert.assertEquals(4, b.getCapacity());

    Assert.assertEquals(0, p0.getSlotId());
    Assert.assertEquals(1, p1.getSlotId());
    Assert.assertEquals(2, p2.getSlotId());
    Assert.assertEquals(3, p3.getSlotId());

    b.remove(p2);
    Assert.assertEquals(3, b.size());
    b.remove(p0);
    Assert.assertEquals(2, b.size());
    b.remove(p3);
    Assert.assertEquals(1, b.size());
    b.add(p2);
    Assert.assertEquals(2, b.size());
    Assert.assertEquals(0, p2.getSlotId());

    b.remove(p0);
    Assert.assertEquals(2, b.size());
    b.add(p0);
    Assert.assertEquals(3, b.size());

    // p1 is still in it's slot
    Assert.assertEquals(2, p0.getSlotId());

    b.remove(p2);
    b.add(p2);
    Assert.assertEquals(3, p2.getSlotId());

  }

}
