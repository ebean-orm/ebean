package com.avaje.ebeaninternal.server.lib.sql;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;

public class TestFreeBuffer extends BaseTestCase {

  @Test
  public void test() {

    FreeConnectionBuffer b = new FreeConnectionBuffer();

    PooledConnection p0 = new PooledConnection("0");
    PooledConnection p1 = new PooledConnection("1");
    PooledConnection p2 = new PooledConnection("2");
    // PooledConnection p3 = new PooledConnection("3");

    Assert.assertEquals(0, b.size());
    Assert.assertEquals(true, b.isEmpty());

    b.add(p0);

    Assert.assertEquals(1, b.size());
    Assert.assertEquals(false, b.isEmpty());

    PooledConnection r0 = b.remove();
    Assert.assertTrue(p0 == r0);

    Assert.assertEquals(0, b.size());
    Assert.assertEquals(true, b.isEmpty());

    b.add(p0);
    b.add(p1);
    b.add(p2);

    Assert.assertEquals(3, b.size());

    PooledConnection r1 = b.remove();
    Assert.assertTrue(p0 == r1);
    PooledConnection r2 = b.remove();
    Assert.assertTrue(p1 == r2);

    Assert.assertEquals(1, b.size());
    b.add(p0);
    Assert.assertEquals(2, b.size());
    PooledConnection r3 = b.remove();
    Assert.assertTrue(p2 == r3);
    Assert.assertEquals(1, b.size());
    PooledConnection r4 = b.remove();
    Assert.assertTrue(p0 == r4);
    Assert.assertEquals(0, b.size());

    b.add(p2);
    b.add(p1);
    b.add(p0);

    Assert.assertEquals(3, b.size());

    PooledConnection r5 = b.remove();
    Assert.assertTrue(p2 == r5);
    Assert.assertEquals(2, b.size());

    PooledConnection r6 = b.remove();
    Assert.assertTrue(p1 == r6);
    Assert.assertEquals(1, b.size());

    PooledConnection r7 = b.remove();
    Assert.assertTrue(p0 == r7);
    Assert.assertEquals(0, b.size());

  }
}
