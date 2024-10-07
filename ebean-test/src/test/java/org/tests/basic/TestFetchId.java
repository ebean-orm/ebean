package org.tests.basic;

import io.ebean.test.LoggedSql;
import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.FutureIds;
import io.ebean.Query;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Order;
import org.tests.model.basic.OrderDetail;
import org.tests.model.basic.ResetBasicData;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;

public class TestFetchId extends BaseTestCase {

  @Test
  public void testFetchId() throws InterruptedException, ExecutionException {

    ResetBasicData.reset();

    Query<Order> query = DB.find(Order.class)
      .setAutoTune(false)
      .fetch("details")
      .where().gt("id", 1)
      .gt("details.id", 0)
      .query();

    LoggedSql.start();
    List<Object> ids = query.findIds();
    assertThat(ids).isNotEmpty();
    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).doesNotContain("order by");
    assertThat(sql.get(0)).doesNotContain("join o_order_detail t1");

    FutureIds<Order> futureIds = query.findFutureIds();

    // wait for all the id's to be fetched
    List<Object> idList = futureIds.get();
    assertThat(idList).isNotEmpty();
  }

  @Test
  public void testFetchIdWithExists() throws InterruptedException, ExecutionException {

    ResetBasicData.reset();

    Query<OrderDetail> subQuery = DB.find(OrderDetail.class)
        .alias("sq")
        .where().raw("details.id = sq.id").query();
    Query<Order> query = DB.find(Order.class)
      .where().exists(subQuery)
      .orderBy("orderDate").query();

    List<Object> ids = query.findIds();
    // TODO: assert(query.getGeneratedSql())
    assertThat(ids).isNotEmpty();
    FutureIds<Order> futureIds = query.findFutureIds();

    // wait for all the id's to be fetched
    List<Object> idList = futureIds.get();
    assertThat(idList).isNotEmpty();
  }

  @Test
  public void testFetchIdWithOrderFormula() throws InterruptedException, ExecutionException {

    ResetBasicData.reset();

    Query<Order> query = DB.find(Order.class).orderBy("totalItems");
    query.findIds();
    // TODO: assert(query.getGeneratedSql())
  }
}
