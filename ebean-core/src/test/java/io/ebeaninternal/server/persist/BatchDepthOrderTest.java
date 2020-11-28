package io.ebeaninternal.server.persist;

import org.junit.Test;

import static org.junit.Assert.*;

public class BatchDepthOrderTest {

  @Test
  public void orderingFor() {

    BatchDepthOrder depthOrder = new BatchDepthOrder();

    assertEquals(0, depthOrder.orderingFor(0));
    assertEquals(1, depthOrder.orderingFor(0));
    assertEquals(2, depthOrder.orderingFor(0));
    assertEquals(100, depthOrder.orderingFor(1));
    assertEquals(101, depthOrder.orderingFor(1));
    assertEquals(200, depthOrder.orderingFor(2));
    assertEquals(3, depthOrder.orderingFor(0));

    assertEquals(-100, depthOrder.orderingFor(-1));
    assertEquals(-99, depthOrder.orderingFor(-1));
    assertEquals(-98, depthOrder.orderingFor(-1));

    assertEquals(-200, depthOrder.orderingFor(-2));
    assertEquals(-199, depthOrder.orderingFor(-2));
  }


  @Test
  public void clear() {

    BatchDepthOrder depthOrder = new BatchDepthOrder();

    assertEquals(0, depthOrder.orderingFor(0));
    assertEquals(1, depthOrder.orderingFor(0));
    assertEquals(100, depthOrder.orderingFor(1));
    assertEquals(101, depthOrder.orderingFor(1));

    depthOrder.clear();

    assertEquals(0, depthOrder.orderingFor(0));
    assertEquals(100, depthOrder.orderingFor(1));
    assertEquals(200, depthOrder.orderingFor(2));
  }

}
