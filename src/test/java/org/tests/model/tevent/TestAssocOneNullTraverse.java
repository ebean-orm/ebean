package org.tests.model.tevent;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.junit.Test;

public class TestAssocOneNullTraverse extends BaseTestCase {

  @Test
  public void test() {

    TEvent event = new TEvent("event");
    Ebean.save(event);

    Ebean.find(TEvent.class)
      .fetch("one.logs")
      .findList();
  }

//  @Test
//  public void testSelectAggregation() {
//
//    Query<TEvent> query = Ebean.find(TEvent.class)
//        .select("id, name")
//        .fetch("one", "count");
//
//    query.findList();
//
//    String sql = query.getGeneratedSql();
//    assertThat(sql).contains("asd");
//  }
}
