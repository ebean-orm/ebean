package io.ebeaninternal.server.query;

import io.ebean.BaseTestCase;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.SpiQuery;
import org.junit.Test;
import org.tests.model.basic.Order;

import static org.junit.Assert.assertEquals;

public class LimitOffsetPagedListTest extends BaseTestCase {

  private SpiEbeanServer server = spiEbeanServer();

  @Test
  public void getPageIndex_when_firstRowsZero() {
    assertEquals(limit(0, 10).getPageIndex(), 0);
  }

  @Test
  public void getPageIndex_when_10_10() {
    assertEquals(limit(10, 10).getPageIndex(), 1);
  }

  @Test
  public void getPageIndex_when_20_10() {
    assertEquals(limit(20, 10).getPageIndex(), 2);
  }

  @Test
  public void getPageIndex_when_1_10() {
    assertEquals(limit(1, 10).getPageIndex(), 1);
  }

  @Test
  public void getPageIndex_when_9_10() {
    assertEquals(limit(1, 10).getPageIndex(), 1);
  }

  @Test
  public void getPageIndex_when_10_4() {
    assertEquals(limit(10, 4).getPageIndex(), 3);
  }

  @Test
  public void getPageIndex_when_10_5() {
    assertEquals(limit(10, 5).getPageIndex(), 2);
  }

  @Test
  public void getPageIndex_when_10_9() {
    assertEquals(limit(10, 9).getPageIndex(), 2);
  }

  @Test
  public void getPageIndex_when_10_11() {
    assertEquals(limit(10, 11).getPageIndex(), 1);
  }

  @Test
  public void getPageIndex_when_21_10() {
    assertEquals(limit(21, 10).getPageIndex(), 3);
  }

  @Test
  public void getPageIndex_when_29_10() {
    assertEquals(limit(29, 10).getPageIndex(), 3);
  }

  @Test
  public void getPageIndex_when_30_10() {
    assertEquals(limit(30, 10).getPageIndex(), 3);
  }

  @Test
  public void getPageIndex_when_31_10() {
    assertEquals(limit(31, 10).getPageIndex(), 4);
  }

  @Test
  public void getPageIndex_when_40_10() {
    assertEquals(limit(40, 10).getPageIndex(), 4);
  }


  private LimitOffsetPagedList<Order> limit(int first, int max) {
    return limitQuery(queryWith(first, max));
  }

  private LimitOffsetPagedList<Order> limitQuery(SpiQuery<Order> query) {
    return new LimitOffsetPagedList<>(server, query);
  }

  private SpiQuery<Order> queryWith(int first, int max) {
    SpiQuery<Order> query = query();
    query.setFirstRow(first);
    query.setMaxRows(max);
    return query;
  }

  private SpiQuery<Order> query() {
    return (SpiQuery<Order>) server.find(Order.class);
  }

}
