package com.avaje.tests.basic;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.FutureIds;
import com.avaje.ebean.Query;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.ResetBasicData;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;

public class TestFetchId extends BaseTestCase {

  @Test
  public void testFetchId() throws InterruptedException, ExecutionException {

    ResetBasicData.reset();

    Query<Order> query = Ebean.find(Order.class)
      .setAutoTune(false)
      .fetch("details")
      .where().gt("id", 1)
      .gt("details.id", 0)
      .query();

    List<Object> ids = Ebean.getServer(null).findIds(query, null);
    assertThat(ids).isNotEmpty();

    FutureIds<Order> futureIds = Ebean.getServer(null).findFutureIds(query, null);

    // wait for all the id's to be fetched
    List<Object> idList = futureIds.get();
    assertThat(idList).isNotEmpty();
  }
}
