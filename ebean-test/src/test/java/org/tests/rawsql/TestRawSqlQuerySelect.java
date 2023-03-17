package org.tests.rawsql;

import io.ebean.DB;
import io.ebean.RawSql;
import io.ebean.RawSqlBuilder;
import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.ResetBasicData;
import org.tests.rawsql.transport.QuerySumResponse;
import org.tests.rawsql.transport.SampleReport;

import java.util.List;

class TestRawSqlQuerySelect extends BaseTestCase {

  @Test
  void testRawSQL() {
    ResetBasicData.reset();

    RawSql rawSql = RawSqlBuilder.parse("select id, name, anniversary, city from (select c.id, c.name, c.anniversary, a.city from o_customer c left join o_address a ON a.id = c.billing_address_id) w").create();
    String sumSql = "COUNT(1) AS count, SUM(id) AS sum";

    var query = DB.find(SampleReport.class);
    query.setRawSql(rawSql);
    query.setMaxRows(10);
    List<SampleReport> list = query.findList();
    Assertions.assertNotNull(list);
    Assertions.assertTrue(!list.isEmpty() && list.size() < 11);

    var countSum = DB.find(SampleReport.class)
      .setRawSql(rawSql)
      //.select(sumSql)
      .select(sumSql)
      .setMaxRows(10)
      .asDto(QuerySumResponse.class)
      .findOne();
    Assertions.assertInstanceOf(QuerySumResponse.class, countSum);
  }
}
