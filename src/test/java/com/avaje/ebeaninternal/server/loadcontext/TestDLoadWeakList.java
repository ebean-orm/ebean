package com.avaje.ebeaninternal.server.loadcontext;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;

public class TestDLoadWeakList extends BaseTestCase {

  @Test
  public void test() {

    String s0 = new String("zero");
    String s1 = new String("one");
    String s2 = new String("two");
    String s3 = new String("three");
    String s4 = new String("four");
    String s5 = new String("five");
    String s6 = new String("six");
    String s7 = new String("seven");
    String s8 = new String("eight");
    String s9 = new String("nine");
    String s10 = new String("ten");

    DLoadWeakList<Object> list = new DLoadWeakList<Object>();

    list.add(s0);
    list.add(s1);
    list.add(s2);
    list.add(s3);
    list.add(s4);
    list.add(s5);
    list.add(s6);
    list.add(s7);
    list.add(s8);
    list.add(s9);
    list.add(s10);

    Assert.assertEquals(11, list.list.size());

    System.gc();

    try {
      Thread.sleep(300);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    // this is really only a HINT, so no guarantee
    // .. but the SUN JVM does do the business
    System.gc();

    Assert.assertEquals(11, list.list.size());

    List<Object> b0 = list.getLoadBatch(0, 2);
    Assert.assertEquals(2, b0.size());
    Assert.assertEquals("zero", b0.get(0));
    Assert.assertEquals("one", b0.get(1));

    try {
      b0 = list.getLoadBatch(0, 2);
      Assert.assertTrue(false);
    } catch (IllegalStateException e) {
      Assert.assertTrue(true);
    }
    b0 = list.getNextBatch(2);
    Assert.assertEquals(2, b0.size());
    Assert.assertEquals("two", b0.get(0));
    Assert.assertEquals("three", b0.get(1));

    list.removeEntry(1);

    b0 = list.getLoadBatch(7, 2);
    Assert.assertEquals(2, b0.size());
    Assert.assertEquals("seven", b0.get(0));
    Assert.assertEquals("eight", b0.get(1));

  }
}
