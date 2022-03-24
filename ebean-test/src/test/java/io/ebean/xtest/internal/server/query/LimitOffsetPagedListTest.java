package io.ebean.xtest.internal.server.query;

import io.ebean.xtest.BaseTestCase;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.server.query.LimitOffsetPagedList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Order;

public class LimitOffsetPagedListTest extends BaseTestCase {

  private SpiEbeanServer server = spiEbeanServer();

  @Test
  public void getPageIndex_when_firstRowsZero() {
    Assertions.assertEquals(limit(0, 10).getPageIndex(), 0);
  }

  @Test
  public void getPageIndex_when_10_10() {
    Assertions.assertEquals(limit(10, 10).getPageIndex(), 1);
  }

  @Test
  public void getPageIndex_when_20_10() {
    Assertions.assertEquals(limit(20, 10).getPageIndex(), 2);
  }

  @Test
  public void getPageIndex_when_1_10() {
    Assertions.assertEquals(limit(1, 10).getPageIndex(), 1);
  }

  @Test
  public void getPageIndex_when_9_10() {
    Assertions.assertEquals(limit(1, 10).getPageIndex(), 1);
  }

  @Test
  public void getPageIndex_when_10_4() {
    Assertions.assertEquals(limit(10, 4).getPageIndex(), 3);
  }

  @Test
  public void getPageIndex_when_10_5() {
    Assertions.assertEquals(limit(10, 5).getPageIndex(), 2);
  }

  @Test
  public void getPageIndex_when_10_9() {
    Assertions.assertEquals(limit(10, 9).getPageIndex(), 2);
  }

  @Test
  public void getPageIndex_when_10_11() {
    Assertions.assertEquals(limit(10, 11).getPageIndex(), 1);
  }

  @Test
  public void getPageIndex_when_21_10() {
    Assertions.assertEquals(limit(21, 10).getPageIndex(), 3);
  }

  @Test
  public void getPageIndex_when_29_10() {
    Assertions.assertEquals(limit(29, 10).getPageIndex(), 3);
  }

  @Test
  public void getPageIndex_when_30_10() {
    Assertions.assertEquals(limit(30, 10).getPageIndex(), 3);
  }

  @Test
  public void getPageIndex_when_31_10() {
    Assertions.assertEquals(limit(31, 10).getPageIndex(), 4);
  }

  @Test
  public void getPageIndex_when_40_10() {
    Assertions.assertEquals(limit(40, 10).getPageIndex(), 4);
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
