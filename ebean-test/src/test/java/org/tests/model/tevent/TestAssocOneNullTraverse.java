package org.tests.model.tevent;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;

public class TestAssocOneNullTraverse extends BaseTestCase {

  @Test
  public void test() {

    TEvent event = new TEvent("event");
    DB.save(event);

    DB.find(TEvent.class)
      .fetch("one.logs")
      .findList();
  }

//  @Test
//  public void testSelectAggregation() {
//
//    Query<TEvent> query = DB.find(TEvent.class)
//        .select("id, name")
//        .fetch("one", "count");
//
//    query.findList();
//
//    String sql = query.getGeneratedSql();
//    assertThat(sql).contains("asd");
//  }
}
