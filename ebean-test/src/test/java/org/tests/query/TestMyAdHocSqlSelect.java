package org.tests.query;

import io.ebean.*;
import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.MyAdHoc;
import org.tests.model.basic.ResetBasicData;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestMyAdHocSqlSelect extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    String sql = "select order_id, count(*) as detailCount from o_order_detail group by order_id";

    RawSql rawSql = RawSqlBuilder
      .parse(sql)
      .columnMapping("order_id", "order.id")
      .create();

    Query<MyAdHoc> query = DB.find(MyAdHoc.class)
      .setRawSql(rawSql)
      .where().gt("order_id", 0)
      .having().gt("detailCount", 0)
      .query();

    assertNotNull(query.findList());
    assertSql(query).contains(" group by order_id  having count(*) > ?");
  }

  @Test
  public void test_when_explicitColumnMapping() {

    ResetBasicData.reset();

    String sql = "select order_id, count(*) as detail_count from o_order_detail group by order_id";

    RawSql rawSql = RawSqlBuilder
      .parse(sql)
      .columnMapping("order_id", "order.id")
      .columnMapping("detail_count", "detailCount")
      .create();

    Query<MyAdHoc> query = DB.find(MyAdHoc.class)
      .setRawSql(rawSql)
      .where().gt("order_id", 0)
      .having().gt("detailCount", 0)
      .query();

    assertNotNull(query.findList());
    assertSql(query).contains(" group by order_id  having count(*) > ?");
  }


}
